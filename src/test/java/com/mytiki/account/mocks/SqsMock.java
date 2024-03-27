/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.mocks;

import org.mockito.Mockito;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

public class SqsMock {
    public static SqsClient mock(String queue) {
        SqsClient sqsClient = Mockito.mock(SqsClient.class);
        Mockito.doReturn(SendMessageResponse.builder().build())
                .when(sqsClient)
                .sendMessage(Mockito.any(SendMessageRequest.class));
        return sqsClient;
    }
}
