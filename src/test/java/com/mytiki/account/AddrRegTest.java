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

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    //TODO add rest of tests. Blocked by fixing AppInfo
}
