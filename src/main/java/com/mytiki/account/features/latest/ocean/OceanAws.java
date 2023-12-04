/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.ocean;

import com.amazonaws.xray.interceptors.TracingInterceptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OceanAws {
    private final SfnClient sfnClient;
    private final S3Client s3Client;
    private final String arn;
    private final ObjectMapper mapper;

    public OceanAws(String region, String arn, ObjectMapper mapper) {
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
                arn, mapper);
    }

    public OceanAws(SfnClient sfnClient, S3Client s3Client, String arn, ObjectMapper mapper) {
        this.arn = arn;
        this.mapper = mapper;
        this.sfnClient = sfnClient;
        this.s3Client = s3Client;
    }

    public String execute(UUID request, String query) {
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

    public OceanStatus status(String executionArn) {
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

    public List<String[]> fetch(String s3Uri) {
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
