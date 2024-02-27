/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.mocks;

import org.mockito.Mockito;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.*;

public class SfnMock {
    public static SfnClient mock(String executionArn) {
        SfnClient sfnClient = Mockito.mock(SfnClient.class);
        Mockito.doReturn(StartExecutionResponse.builder().executionArn(executionArn).build())
                .when(sfnClient)
                .startExecution(Mockito.any(StartExecutionRequest.class));
        Mockito.doReturn(DescribeExecutionResponse.builder().status(ExecutionStatus.SUCCEEDED).build())
                .when(sfnClient)
                .describeExecution(Mockito.any(DescribeExecutionRequest.class));
        return sfnClient;
    }
}
