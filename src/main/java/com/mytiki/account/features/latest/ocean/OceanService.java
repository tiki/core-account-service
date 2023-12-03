/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.ocean;

import com.amazonaws.xray.interceptors.TracingInterceptor;
import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.subscription.SubscriptionDO;
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
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.*;

@XRayEnabled
public class OceanService {
    protected static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final OceanAws aws;
    private final String bucket;
    private final ObjectMapper mapper;
    private final OceanRepository repository;

    public OceanService(OceanAws aws, String bucket, ObjectMapper mapper, OceanRepository repository) {
        this.aws = aws;
        this.bucket = bucket;
        this.mapper = mapper;
        this.repository = repository;
    }

    public OceanDO count(String query) {
        return request(OceanType.COUNT, OceanQuery.count(query));
    }

    public OceanDO sample(String query) {
        return request(OceanType.SAMPLE, OceanQuery.sample(query));
    }

    public OceanDO database(String cleanroomId) {
        return request(OceanType.DATABASE, OceanQuery.database(cleanroomId));
    }

    public OceanDO ctas(String cleanroomId, String table, String query) {
        return request(OceanType.CREATE, OceanQuery.ctas(query, bucket, cleanroomId, table));
    }

    public OceanDO get(String requestId) {
        Optional<OceanDO> found = repository.findByRequestId(UUID.fromString(requestId));
        if(found.isPresent()) {
            if(found.get().getStatus() == OceanStatus.PENDING){
                OceanDO update = found.get();
                update.setStatus(aws.status(update.getExecutionArn()));
                update.setModified(ZonedDateTime.now());
                return repository.save(update);
            }else return found.get();
        }else return null;
    }

    public void callback(OceanAOReq req) {
        UUID requestId = UUID.fromString(req.getRequestId());
        Optional<OceanDO> found = repository.findByRequestId(requestId);
        if(found.isPresent()){
            OceanDO ocean = found.get();
            ocean.setStatus(OceanStatus.SUCCESS);
            ocean.setResultUri(req.getResultUri());
            switch(found.get().getType()) {
                case COUNT, SAMPLE -> {
                    if(req.getResultUri() != null) {
                        try {
                            List<String[]> res = aws.fetch(req.getResultUri());
                            ocean.setResult(mapper.writeValueAsString(res));
                        } catch (ApiException | JsonProcessingException e) {
                            logger.warn("Failed to retrieve results. Skipping", e);
                        }
                    }
                }
                case CREATE -> {}
                case DATABASE, UPDATE -> {}
            }
            ocean.setModified(ZonedDateTime.now());
            repository.save(ocean);
        }else {
            logger.warn("Skipping. Invalid request id: " + req.getRequestId());
        }
    }

    private OceanDO request(OceanType type, String query) {
        UUID requestId = UUID.randomUUID();
        String executionArn = aws.execute(requestId, query);
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
}
