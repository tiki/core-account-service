/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.ocean;

import com.amazonaws.xray.interceptors.TracingInterceptor;
import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import com.mytiki.account.utilities.error.ApiException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.*;

@XRayEnabled
public class OceanService {
    protected static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SfnClient sfnClient;
    private final S3Client s3Client;
    private final String arn;
    private final ObjectMapper mapper;
    private final OceanRepository repository;

    public OceanService(String region, String arn, ObjectMapper mapper, OceanRepository repository) {
        this(SfnClient.builder()
                        .region(Region.of(region))
                        .overrideConfiguration(ClientOverrideConfiguration
                                .builder()
                                .addExecutionInterceptor(new TracingInterceptor())
                                .build())
                        .build(),
                S3Client.builder()
                        .region(Region.of(region))
                        .overrideConfiguration(ClientOverrideConfiguration
                                .builder()
                                .addExecutionInterceptor(new TracingInterceptor())
                                .build())
                        .build(),
                arn, mapper, repository);
    }

    public OceanService(
            SfnClient sfnClient,
            S3Client s3Client,
            String arn,
            ObjectMapper mapper,
            OceanRepository repository) {
        this.arn = arn;
        this.mapper = mapper;
        this.repository = repository;
        this.sfnClient = sfnClient;
        this.s3Client = s3Client;
    }

    public OceanDO query(OceanType type, String query) {
        UUID requestId = UUID.randomUUID();
        String executionArn = execute(requestId, query);
        OceanDO ocean = new OceanDO();
        ZonedDateTime now = ZonedDateTime.now();
        ocean.setRequestId(requestId);
        ocean.setType(type);
        ocean.setStatus(OceanStatus.PENDING);
        ocean.setExecutionArn(executionArn);
        ocean.setCreated(now);
        ocean.setModified(now);
        return repository.save(ocean);
    }

    public void update(OceanAO req) {
        UUID requestId = UUID.fromString(req.getRequestId());
        Optional<OceanDO> found = repository.findByRequestId(requestId);
        if(found.isPresent()){
            OceanDO ocean = found.get();
            ocean.setStatus(OceanStatus.SUCCESS);
            ocean.setResultUri(req.getResultUri());
            try {
                List<String[]> res = fetch(req.getResultUri());
                ocean.setResult(mapper.writeValueAsString(res));
            }catch (ApiException | JsonProcessingException e) {
                logger.warn("Failed to retrieve results. Skipping", e);
            }
            ocean.setModified(ZonedDateTime.now());
            repository.save(ocean);
        } else {
            logger.warn("Skipping. Invalid request id: " + req.getRequestId());
        }
    }

    public OceanDO get(String requestId) {
        Optional<OceanDO> found = repository.findByRequestId(UUID.fromString(requestId));
        if(found.isPresent()) {
            if(found.get().getStatus() == OceanStatus.PENDING){
                OceanDO update = found.get();
                update.setStatus(status(update.getExecutionArn()));
                update.setModified(ZonedDateTime.now());
                return repository.save(update);
            }else return found.get();
        }else return null;
    }

    private String execute(UUID request, String query) {
        String requestId = request.toString();
        Map<String, String> input = new HashMap<>(){{
            put("Query", query);
            put("RequestId", requestId);
        }};
        try {
            StartExecutionRequest executionRequest = StartExecutionRequest.builder()
                    .input(mapper.writeValueAsString(input))
                    .stateMachineArn(arn)
                    .name(requestId)
                    .build();
            StartExecutionResponse rsp = sfnClient.startExecution(executionRequest);
            return rsp.executionArn();
        } catch (JsonProcessingException | SfnException e) {
            throw new ErrorBuilder(HttpStatus.EXPECTATION_FAILED)
                    .message("Query failed to execute")
                    .cause(e)
                    .exception();
        }
    }

    private OceanStatus status(String executionArn) {
        try {
            DescribeExecutionRequest executionRequest = DescribeExecutionRequest.builder()
                .executionArn(executionArn)
                .build();
            DescribeExecutionResponse response = sfnClient.describeExecution(executionRequest);
            return switch(response.status()){
                case SUCCEEDED -> OceanStatus.SUCCESS;
                case RUNNING, PENDING_REDRIVE -> OceanStatus.PENDING;
                case FAILED, TIMED_OUT, ABORTED, UNKNOWN_TO_SDK_VERSION -> OceanStatus.FAILED;
            };
        } catch (SfnException e) {
            throw new ErrorBuilder(HttpStatus.EXPECTATION_FAILED)
                    .message("Failed to retrieve query status")
                    .cause(e)
                    .exception();
        }
    }

    private List<String[]> fetch(String s3Uri) {
        try {
            S3Uri uri = s3Client.utilities().parseUri(new URI(s3Uri));
            if(uri.bucket().isEmpty() || uri.key().isEmpty())
                throw new URISyntaxException(s3Uri, "Bucket and/or Key missing");

            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(uri.bucket().get())
                    .key(uri.key().get())
                    .build();
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(objectRequest);
            CSVReader reader = new CSVReader(new InputStreamReader(objectBytes.asInputStream()));
            return reader.readAll();
        }catch (URISyntaxException | IOException | CsvException e){
            throw new ErrorBuilder(HttpStatus.BAD_REQUEST)
                    .message("Failed to fetch URI")
                    .properties("URI", s3Uri)
                    .cause(e)
                    .exception();
        }
    }
}
