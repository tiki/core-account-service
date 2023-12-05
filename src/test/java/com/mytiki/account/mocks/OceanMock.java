/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.mocks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.ocean.OceanAws;
import org.mockito.Mockito;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.*;

import java.nio.charset.StandardCharsets;

public class OceanMock {

    public static OceanAws aws() {
        OceanAws oceanAws = Mockito.mock(OceanAws.class);
        Mockito.doReturn("dummy").when(oceanAws).execute(Mockito.any(), Mockito.any());
        return oceanAws;
    }

    public static OceanAws aws(String executionArn, String arn, ObjectMapper mapper){
        SfnClient sfnClient = Mockito.mock(SfnClient.class);
        Mockito.doReturn(StartExecutionResponse.builder().executionArn(executionArn).build())
                .when(sfnClient)
                .startExecution(Mockito.any(StartExecutionRequest.class));
        Mockito.doReturn(DescribeExecutionResponse.builder().status(ExecutionStatus.SUCCEEDED).build())
                .when(sfnClient)
                .describeExecution(Mockito.any(DescribeExecutionRequest.class));
        S3Client s3Client = Mockito.mock(S3Client.class);
        Mockito.doReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(),
                        "hello,world".getBytes(StandardCharsets.UTF_8)))
                .when(s3Client)
                .getObjectAsBytes(Mockito.any(GetObjectRequest.class));
        return new OceanAws(sfnClient, s3Client, arn, mapper);
    }
}