package com.mytiki.account;

import com.mytiki.account.features.latest.addr_reg.AddrRegAOReq;
import com.mytiki.account.features.latest.addr_reg.AddrRegAORsp;
import com.mytiki.account.features.latest.addr_reg.AddrRegService;
import com.mytiki.account.features.latest.app_info.AppInfoAO;
import com.mytiki.account.features.latest.app_info.AppInfoService;
import com.mytiki.account.features.latest.user_info.UserInfoAO;
import com.mytiki.account.features.latest.user_info.UserInfoService;
import com.mytiki.account.fixtures.AddrFixture;
import com.mytiki.account.main.App;
import com.nimbusds.jose.JOSEException;
import org.bouncycastle.crypto.CryptoException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {App.class}
)
@ActiveProfiles(profiles = {"ci", "dev", "local"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AddrRegTest {

    @Autowired
    private AddrRegService service;

    @Autowired
    private AppInfoService appInfo;

    @Autowired
    private UserInfoService userInfo;

    @Test
    public void Test_RegisterNew_Success() throws JOSEException, NoSuchAlgorithmException, CryptoException {
        UserInfoAO user = userInfo.createIfNotExists(UUID.randomUUID() + "@test.com");
        AppInfoAO app = appInfo.create(UUID.randomUUID().toString(), user.getUserId());

        String id = UUID.randomUUID().toString();
        AddrRegAOReq req = AddrFixture.req(id);
        AddrRegAORsp rsp = service.register(app.getAppId(), req, null);

        assertEquals(rsp.getAddress(), req.getAddress());
        assertEquals(rsp.getPubKey(), req.getPubKey());
        assertEquals(rsp.getId(), req.getId());
        assertNotNull(rsp.getCreated());
    }

    @Test
    public void Test_RegisterTwo_Success() throws JOSEException, NoSuchAlgorithmException, CryptoException {
        UserInfoAO user = userInfo.createIfNotExists(UUID.randomUUID() + "@test.com");
        AppInfoAO app = appInfo.create(UUID.randomUUID().toString(), user.getUserId());

        String id = UUID.randomUUID().toString();
        AddrRegAOReq req1 = AddrFixture.req(id);
        AddrRegAORsp rsp1 = service.register(app.getAppId(), req1, null);
        AddrRegAOReq req2 = AddrFixture.req(id);
        AddrRegAORsp rsp2 = service.register(app.getAppId(), req2, null);

        assertEquals(rsp1.getId(), rsp2.getId());
    }

    @Test
    public void Test_Get_Success() throws JOSEException, NoSuchAlgorithmException, CryptoException {
        UserInfoAO user = userInfo.createIfNotExists(UUID.randomUUID() + "@test.com");
        AppInfoAO app = appInfo.create(UUID.randomUUID().toString(), user.getUserId());

        String id = UUID.randomUUID().toString();
        AddrRegAOReq req = AddrFixture.req(id);
        service.register(app.getAppId(), req, null);
        AddrRegAORsp rsp = service.get(app.getAppId(), req.getAddress());

        assertEquals(rsp.getAddress(), req.getAddress());
        assertEquals(rsp.getPubKey(), req.getPubKey());
        assertEquals(rsp.getId(), req.getId());
        assertNotNull(rsp.getCreated());
    }

    @Test
    public void Test_GetAll_Success() throws NoSuchAlgorithmException, CryptoException, JOSEException {
        UserInfoAO user = userInfo.createIfNotExists(UUID.randomUUID() + "@test.com");
        AppInfoAO app = appInfo.create(UUID.randomUUID().toString(), user.getUserId());

        String id = UUID.randomUUID().toString();
        service.register(app.getAppId(), AddrFixture.req(id), null);
        service.register(app.getAppId(), AddrFixture.req(id), null);

        List<AddrRegAORsp> rspList = service.getAll(app.getAppId(), id);
        assertEquals(rspList.size(), 2);
    }

    @Test
    @Transactional
    public void Test_Delete_Success() throws NoSuchAlgorithmException, CryptoException, JOSEException {
        UserInfoAO user = userInfo.createIfNotExists(UUID.randomUUID() + "@test.com");
        AppInfoAO app = appInfo.create(UUID.randomUUID().toString(), user.getUserId());

        String id = UUID.randomUUID().toString();
        AddrRegAOReq req = AddrFixture.req(id);
        service.register(app.getAppId(), req, null);
        service.delete(app.getAppId(), req.getAddress());
        AddrRegAORsp rsp = service.get(app.getAppId(), req.getAddress());
        assertNull(rsp.getId());
    }

    @Test
    @Transactional
    public void Test_DeleteAll_Success() throws NoSuchAlgorithmException, CryptoException, JOSEException {
        UserInfoAO user = userInfo.createIfNotExists(UUID.randomUUID() + "@test.com");
        AppInfoAO app = appInfo.create(UUID.randomUUID().toString(), user.getUserId());

        String id = UUID.randomUUID().toString();
        service.register(app.getAppId(), AddrFixture.req(id), null);
        service.register(app.getAppId(), AddrFixture.req(id), null);

        service.deleteAll(app.getAppId(), id);
        List<AddrRegAORsp> rsp = service.getAll(app.getAppId(), id);
        assertTrue(rsp.isEmpty());
    }
}
