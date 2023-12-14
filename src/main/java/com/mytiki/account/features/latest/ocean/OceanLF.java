/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.features.latest.ocean;

import com.amazonaws.xray.interceptors.TracingInterceptor;
import com.mytiki.account.features.latest.cleanroom.CleanroomDO;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lakeformation.LakeFormationClient;
import software.amazon.awssdk.services.lakeformation.model.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class OceanLF {
    private static final String INTERNAL_TAG = "internal";
    private static final String INTERNAL_TAG_READ = "read";
    private static final String INTERNAL_TAG_WRITE = "write";
    public final LakeFormationClient lfClient;
    private final String catalogId;
    private final String locationRoleArn;
    private final String adminRoleArn;
    private final String execRoleArn;
    private final String bucket;

    public OceanLF(
            String region,
            String catalogId,
            String locationRoleArn,
            String adminRoleArn,
            String execRoleArn,
            String bucket) {
        this(LakeFormationClient.builder()
                        .region(Region.of(region))
                        .overrideConfiguration(ClientOverrideConfiguration
                                .builder()
                                .addExecutionInterceptor(new TracingInterceptor())
                                .build())
                        .build(),
                catalogId, locationRoleArn, adminRoleArn, execRoleArn, bucket);
    }

    public OceanLF(
            LakeFormationClient lfClient,
            String catalogId,
            String locationRoleArn,
            String adminRoleArn,
            String execRoleArn,
            String bucket) {
        this.lfClient = lfClient;
        this.catalogId = catalogId;
        this.locationRoleArn = locationRoleArn;
        this.adminRoleArn = adminRoleArn;
        this.execRoleArn = execRoleArn;
        this.bucket = bucket;
    }

    public void add(CleanroomDO cleanroom){
        String orgId = cleanroom.getOrg().getOrgId().toString();
        String cleanroomId = cleanroom.getCleanroomId().toString();
        String arn = "arn:aws:s3:::" + bucket + "/cleanroom/" + cleanroomId;

        Optional<List<String>> vals = getTag(orgId);
        if(vals.isEmpty()) { createTag(orgId, List.of(cleanroomId)); }
        else if(!vals.get().contains(cleanroomId)) { updateTag(orgId, List.of(cleanroomId), null); }

        register(arn);
        grantLocation(cleanroom.getAws(), arn, List.of(Permission.DATA_LOCATION_ACCESS));
        grantLocation(execRoleArn, arn, List.of(Permission.DATA_LOCATION_ACCESS));

        addTag(cleanroom.getName(), orgId, cleanroomId);
        addTag(cleanroom.getName(), INTERNAL_TAG, INTERNAL_TAG_READ);

        grantDatabase(cleanroom.getAws(), cleanroom.getName(), orgId, List.of(cleanroomId),
                List.of(Permission.DESCRIBE));
        grantTable(cleanroom.getAws(), cleanroom.getName(), orgId, List.of(cleanroomId),
                List.of(Permission.DESCRIBE, Permission.SELECT));
        grantDatabase(execRoleArn, cleanroom.getName(), INTERNAL_TAG,
                List.of(INTERNAL_TAG_READ, INTERNAL_TAG_WRITE), List.of(Permission.ALL));
        grantTable(execRoleArn, cleanroom.getName(), INTERNAL_TAG,
                List.of(INTERNAL_TAG_READ, INTERNAL_TAG_WRITE), List.of(Permission.ALL));
        grantDatabase(adminRoleArn, cleanroom.getName(), INTERNAL_TAG,
                List.of(INTERNAL_TAG_READ), List.of(Permission.DESCRIBE));
        grantTable(adminRoleArn, cleanroom.getName(), INTERNAL_TAG,
                List.of(INTERNAL_TAG_READ), List.of(Permission.SELECT, Permission.DESCRIBE));
    }

    private void createTag(String tagKey, List<String> tagValues) throws AwsServiceException, SdkClientException {
        CreateLfTagRequest request = CreateLfTagRequest.builder()
                .catalogId(catalogId)
                .tagKey(tagKey)
                .tagValues(tagValues)
                .build();
        lfClient.createLFTag(request);
    }

    private void updateTag(
            String tagKey,
            List<String> valuesToAdd,
            List<String> valuesToRemove
    ) throws AwsServiceException, SdkClientException {
        UpdateLfTagRequest.Builder request = UpdateLfTagRequest.builder()
                .catalogId(catalogId)
                .tagKey(tagKey);
        if(valuesToAdd != null && !valuesToAdd.isEmpty()) request.tagValuesToAdd(valuesToAdd);
        if(valuesToRemove != null && !valuesToRemove.isEmpty()) request.tagValuesToDelete(valuesToRemove);
        lfClient.updateLFTag(request.build());
    }

    private Optional<List<String>> getTag(String tagKey) throws AwsServiceException, SdkClientException {
        GetLfTagRequest request = GetLfTagRequest.builder()
                .catalogId(catalogId)
                .tagKey(tagKey)
                .build();
        try {
            GetLfTagResponse response = lfClient.getLFTag(request);
            return Optional.of(response.tagValues());
        }catch (EntityNotFoundException nf){
            return Optional.empty();
        }
    }

    private void register(String arn) throws AwsServiceException, SdkClientException {
        RegisterResourceRequest request = RegisterResourceRequest.builder()
                .resourceArn(arn)
                .hybridAccessEnabled(false)
                .roleArn(locationRoleArn)
                .build();
        lfClient.registerResource(request);
    }

    private void grantDatabase(
            String principal,
            String database,
            String tagKey,
            List<String> tagValues,
            List<Permission> permissions
    ) throws AwsServiceException, SdkClientException {
        GrantPermissionsRequest request = GrantPermissionsRequest.builder()
                .catalogId(catalogId)
                .resource(Resource.builder()
                        .database(DatabaseResource.builder()
                                .catalogId(catalogId)
                                .name(database)
                                .build())
                        .lfTag(LFTagKeyResource.builder()
                                .catalogId(catalogId)
                                .tagKey(tagKey)
                                .tagValues(tagValues)
                                .build())
                        .build())
                .principal(DataLakePrincipal.builder()
                        .dataLakePrincipalIdentifier(principal)
                        .build())
                .permissions(permissions)
                .build();
        lfClient.grantPermissions(request);
    }

    private void grantTable(
            String principal,
            String database,
            String tagKey,
            List<String> tagValues,
            List<Permission> permissions
    ) throws AwsServiceException, SdkClientException {
        GrantPermissionsRequest request = GrantPermissionsRequest.builder()
                .catalogId(catalogId)
                .resource(Resource.builder()
                        .table(TableResource.builder()
                                .catalogId(catalogId)
                                .databaseName(database)
                                .tableWildcard(TableWildcard.builder().build())
                                .build())
                        .lfTag(LFTagKeyResource.builder()
                                .catalogId(catalogId)
                                .tagKey(tagKey)
                                .tagValues(tagValues)
                                .build())
                        .build())
                .principal(DataLakePrincipal.builder()
                        .dataLakePrincipalIdentifier(principal)
                        .build())
                .permissions(permissions)
                .build();
        lfClient.grantPermissions(request);
    }

    private void grantLocation(
            String principal,
            String arn,
            List<Permission> permissions
    ) throws AwsServiceException, SdkClientException {
        GrantPermissionsRequest request = GrantPermissionsRequest.builder()
                .catalogId(catalogId)
                .resource(Resource.builder()
                        .dataLocation(DataLocationResource.builder()
                                .catalogId(catalogId)
                                .resourceArn(arn)
                                .build())
                        .build())
                .principal(DataLakePrincipal.builder()
                        .dataLakePrincipalIdentifier(principal)
                        .build())
                .permissions(permissions)
                .build();
        lfClient.grantPermissions(request);
    }

    private void addTag(
            String database,
            String tagKey,
            String tagValue
    ) throws AwsServiceException, SdkClientException {
        AddLfTagsToResourceRequest request = AddLfTagsToResourceRequest.builder()
                .catalogId(catalogId)
                .resource(Resource.builder()
                        .database(DatabaseResource.builder()
                                .catalogId(catalogId)
                                .name(database)
                                .build())
                        .build())
                .lfTags(LFTagPair.builder()
                        .catalogId(catalogId)
                        .tagKey(tagKey)
                        .tagValues(tagValue)
                        .build())
                .build();
        lfClient.addLFTagsToResource(request);
    }
}
