package com.mytiki.account;

import com.mytiki.account.features.latest.jwks.JwksDO;
import com.mytiki.account.features.latest.jwks.JwksRepository;
import com.mytiki.account.features.latest.jwks.JwksService;
import com.mytiki.account.fixtures.JwksFixture;
import com.mytiki.account.main.App;
import com.mytiki.spring_rest_api.ApiException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {App.class}
)
@ActiveProfiles(profiles = {"ci", "dev", "local"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JwksTest {
    @Autowired
    private JwksRepository repository;
    @Autowired
    private TestRestTemplate testRestTemplate;
    private MockRestServiceServer mockServer;
    private JwksService service;

    @BeforeEach
    public void init() {
        mockServer = MockRestServiceServer.createServer(testRestTemplate.getRestTemplate());
        service = new JwksService(repository, testRestTemplate.getRestTemplate(), 0);
    }

    @Test
    public void Test_No_Cache_Success() throws URISyntaxException {
        JwksDO jwks = service.cache(JwksFixture.endpoint, true);
        assertEquals(jwks.getEndpoint().toString(), new URI(JwksFixture.endpoint).toString());
        assertEquals(jwks.getVerifySub(), true);
        assertNotNull(jwks.getModified());
        assertNotNull(jwks.getCreated());
    }

    @Test
    public void Test_Cache_Success() throws URISyntaxException {
        URI endpoint = new URI(JwksFixture.endpoint);
        mockServer.expect(requestTo(endpoint)).andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JwksFixture.ES256));
        service = new JwksService(repository, testRestTemplate.getRestTemplate(), 1000);

        service.cache(JwksFixture.endpoint, true);
        Optional<JwksDO> first = service.get(endpoint);
        Optional<JwksDO> second = service.get(endpoint);

        assertTrue(first.isPresent());
        assertTrue(second.isPresent());
        assertEquals(first.get().getModified(), second.get().getModified());
    }

    @Test
    public void Test_Fetch_Success() throws URISyntaxException, ParseException {
        URI endpoint = new URI(JwksFixture.endpoint);
        mockServer.expect(requestTo(endpoint)).andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JwksFixture.ES256));
        service.cache(JwksFixture.endpoint, false);
        Optional<JwksDO> jwks = service.get(endpoint);
        assertTrue(jwks.isPresent());
        assertEquals(jwks.get().getEndpoint().toString(), endpoint.toString());
        assertEquals(jwks.get().getKeySet().toString(), JWKSet.parse(JwksFixture.ES256).toString());
        assertNotNull(jwks.get().getModified());
        assertNotNull(jwks.get().getCreated());
    }

    @Test
    public void Test_Guard_Success() throws URISyntaxException, ParseException, JOSEException {
        URI endpoint = new URI(JwksFixture.endpoint);
        mockServer.expect(requestTo(endpoint)).andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JwksFixture.RS256));
        service.cache(JwksFixture.endpoint, false);

        String jwt = JwksFixture.jwt(JwksFixture.RS256, null);
        service.guard(endpoint, jwt);
    }

    @Test
    public void Test_Guard_Sub_Success() throws URISyntaxException, ParseException, JOSEException {
        URI endpoint = new URI(JwksFixture.endpoint);
        mockServer.expect(requestTo(endpoint)).andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JwksFixture.RS256));
        service.cache(JwksFixture.endpoint, true);

        String sub = UUID.randomUUID().toString();
        String jwt = JwksFixture.jwt(JwksFixture.RS256, sub);
        service.guard(endpoint, jwt, sub);
    }

    @Test
    public void Test_Guard_NoKeys_Failure() throws URISyntaxException, ParseException, JOSEException {
        URI endpoint = new URI("https://" + UUID.randomUUID() + ".com");
        service.cache(JwksFixture.endpoint, false);
        String jwt = JwksFixture.jwt(JwksFixture.RS256, null);
        ApiException ex = assertThrows(ApiException.class, () ->  service.guard(endpoint, jwt));
        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
    }

    @Test
    public void Test_Guard_NoEndpoint_Failure() throws URISyntaxException, ParseException, JOSEException {
        URI endpoint = new URI("https://" + UUID.randomUUID() + ".com");
        String jwt = JwksFixture.jwt(JwksFixture.RS256, null);
        ApiException ex = assertThrows(ApiException.class, () ->  service.guard(endpoint, jwt));
        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
    }
}
