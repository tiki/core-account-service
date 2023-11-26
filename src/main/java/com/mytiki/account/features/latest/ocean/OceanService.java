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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.*;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@XRayEnabled
public class OceanService {
    protected static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SfnClient client;
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
                arn, mapper, repository);
    }

    public OceanService(SfnClient client, String arn, ObjectMapper mapper, OceanRepository repository) {
        this.arn = arn;
        this.mapper = mapper;
        this.repository = repository;
        this.client = client;
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
            //go get the result from s3 and update the DB.
            ocean.setModified(ZonedDateTime.now());
            repository.save(ocean);
        } else {
            logger.warn("Skipping. Invalid request id: " + req.getRequestId());
        }
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
            StartExecutionResponse rsp = client.startExecution(executionRequest);
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
            DescribeExecutionResponse response = client.describeExecution(executionRequest);
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
}
