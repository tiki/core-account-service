package com.mytiki.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytiki.account.features.latest.cleanroom.CleanroomDO;
import com.mytiki.account.features.latest.cleanroom.CleanroomRepository;
import com.mytiki.account.features.latest.cleanroom.CleanroomService;
import com.mytiki.account.features.latest.oauth.OauthSub;
import com.mytiki.account.features.latest.oauth.OauthSubNamespace;
import com.mytiki.account.features.latest.ocean.OceanDO;
import com.mytiki.account.features.latest.ocean.OceanRepository;
import com.mytiki.account.features.latest.ocean.OceanSF;
import com.mytiki.account.features.latest.ocean.OceanService;
import com.mytiki.account.features.latest.org.OrgDO;
import com.mytiki.account.features.latest.org.OrgRepository;
import com.mytiki.account.features.latest.profile.ProfileDO;
import com.mytiki.account.features.latest.profile.ProfileRepository;
import com.mytiki.account.features.latest.profile.ProfileService;
import com.mytiki.account.features.latest.provider.ProviderDO;
import com.mytiki.account.features.latest.subscription.*;
import com.mytiki.account.main.App;
import com.mytiki.account.mocks.JwtMock;
import com.mytiki.account.mocks.OceanMock;
import com.mytiki.account.mocks.StripeMock;
import com.mytiki.account.utilities.facade.StripeF;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {App.class, JwtMock.class}
)
@ActiveProfiles(profiles = {"ci", "dev", "local"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SubscriptionTest {
    @Value("${com.mytiki.account.ocean.arn.state}")
    private String arn;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private OceanRepository repository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private CleanroomRepository cleanroomRepository;
    @Autowired
    private OrgRepository orgRepository;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private ProfileService userInfo;
    private OceanService oceanService;
    private SubscriptionService subscriptionService;
    private CleanroomService cleanroomService;
    private final String executionArn = "dummy-execution-arn";
    @BeforeEach
    public void before() throws StripeException {
        OceanSF sf = OceanMock.sf(executionArn, arn, mapper);
        StripeF stripe = StripeMock.facade();
        this.oceanService = new OceanService(sf, OceanMock.lf(), "dummy", mapper, repository, StripeMock.facade());
        this.cleanroomService = new CleanroomService(cleanroomRepository, profileService, this.oceanService);
        this.subscriptionService = new SubscriptionService(subscriptionRepository, oceanService, cleanroomService, stripe);

    }

    @Test
    @Transactional
     public void Test_Pause_Success(){
        OrgDO org = new OrgDO();
        org.setOrgId(UUID.randomUUID());
        org.setBillingId("test_billing");
        org.setCreated(ZonedDateTime.now());
        org.setModified(ZonedDateTime.now());
        OrgDO savedOrg = orgRepository.save(org);

        CleanroomDO cleanroom = new CleanroomDO();
        cleanroom.setCleanroomId(UUID.randomUUID());
        cleanroom.setName("test_cleanroom");
        cleanroom.setAws("test_aws");
        cleanroom.setDescription("test_description");
        cleanroom.setOrg(savedOrg);
        cleanroom.setCreated(ZonedDateTime.now());
        cleanroom.setModified(ZonedDateTime.now());
        CleanroomDO savedCleanroom = cleanroomRepository.save(cleanroom);

        SubscriptionDO subscription = new SubscriptionDO();
        subscription.setSubscriptionId(UUID.randomUUID());
        subscription.setStatus(SubscriptionStatus.SUBSCRIBED);
        subscription.setName("dummy");
        subscription.setCleanroom(cleanroom);
        subscription.setQuery("dummy query");
        subscription.setCreated(ZonedDateTime.now());
        subscription.setModified(ZonedDateTime.now());
        SubscriptionDO savedSubscription = subscriptionRepository.save(subscription);

        subscriptionService.pause(new OauthSub(OauthSubNamespace.INTERNAL, UUID.randomUUID().toString()), savedSubscription.getSubscriptionId().toString());
        Optional<SubscriptionDO> sub = subscriptionRepository.findBySubscriptionId(savedSubscription.getSubscriptionId());

        sub.ifPresent(subscriptionDO -> assertSame(subscriptionDO.getStatus(), SubscriptionStatus.STOPPED));
    }

    @Test
    @Transactional
    public void Test_Restart_Success(){
        OrgDO org = new OrgDO();
        org.setOrgId(UUID.randomUUID());
        org.setBillingId("test_billing");
        org.setCreated(ZonedDateTime.now());
        org.setModified(ZonedDateTime.now());
        OrgDO savedOrg = orgRepository.save(org);

        CleanroomDO cleanroom = new CleanroomDO();
        cleanroom.setCleanroomId(UUID.randomUUID());
        cleanroom.setName("test_cleanroom");
        cleanroom.setAws("test_aws");
        cleanroom.setDescription("test_description");
        cleanroom.setOrg(savedOrg);
        cleanroom.setCreated(ZonedDateTime.now());
        cleanroom.setModified(ZonedDateTime.now());
        CleanroomDO savedCleanroom = cleanroomRepository.save(cleanroom);

        SubscriptionDO subscription = new SubscriptionDO();
        subscription.setSubscriptionId(UUID.randomUUID());
        subscription.setStatus(SubscriptionStatus.STOPPED);
        subscription.setName("dummy");
        subscription.setCleanroom(cleanroom);
        subscription.setQuery("dummy query");
        subscription.setCreated(ZonedDateTime.now());
        subscription.setModified(ZonedDateTime.now());
        SubscriptionDO savedSubscription = subscriptionRepository.save(subscription);

        subscriptionService.restart(new OauthSub(OauthSubNamespace.INTERNAL, UUID.randomUUID().toString()), savedSubscription.getSubscriptionId().toString());
        Optional<SubscriptionDO> sub = subscriptionRepository.findBySubscriptionId(savedSubscription.getSubscriptionId());

        sub.ifPresent(subscriptionDO -> assertSame(subscriptionDO.getStatus(), SubscriptionStatus.SUBSCRIBED));
    }
}
