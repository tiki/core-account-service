/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.facade;

import com.amazonaws.xray.interceptors.TracingInterceptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.utilities.builder.ErrorBuilder;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.SfnException;
import software.amazon.awssdk.services.sfn.model.StartExecutionRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SubscriptionF {
    private SfnClient client;
    private String arn;
    private ObjectMapper mapper;

    public SubscriptionF(String region, String arn) {
        this.arn = arn;
        this.mapper = new ObjectMapper();
        this.client = SfnClient.builder()
                .region(Region.of(region))
                .overrideConfiguration(
                        ClientOverrideConfiguration
                                .builder()
                                .addExecutionInterceptor(new TracingInterceptor())
                                .build())
                .build();
    }

    public String execute(String query) {
        String requestId = UUID.randomUUID().toString();
        Map<String, String> request = new HashMap<>(){{
            put("Query", query);
            put("RequestId", requestId);
        }};
        try {
            StartExecutionRequest executionRequest = StartExecutionRequest.builder()
                    .input(mapper.writeValueAsString(request))
                    .stateMachineArn(arn)
                    .name(requestId)
                    .build();
            client.startExecution(executionRequest);
            return requestId;
        } catch (JsonProcessingException | SfnException e) {
            throw new ErrorBuilder(HttpStatus.EXPECTATION_FAILED)
                    .message("Query failed to execute")
                    .cause(e)
                    .exception();
        }
    }
}
