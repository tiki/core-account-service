/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.facade;

import com.amazonaws.xray.interceptors.TracingInterceptor;
import com.amazonaws.xray.spring.aop.XRayEnabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.lang.invoke.MethodHandles;

@XRayEnabled
public class SqsF {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SqsClient sqs;
    private final String queue;

    public SqsF(String region, String queue) {
        this.queue = queue;
        sqs = SqsClient.builder()
                .overrideConfiguration(ClientOverrideConfiguration
                        .builder()
                        .addExecutionInterceptor(new TracingInterceptor())
                        .build())
                .region(Region.of(region)).build();
    }

    public void send(String groupId, String body) {
        SendMessageRequest request = SendMessageRequest.builder()
                .messageGroupId(groupId)
                .messageBody(body)
                .queueUrl(queue)
                .build();
        sqs.sendMessage(request);
    }
}
