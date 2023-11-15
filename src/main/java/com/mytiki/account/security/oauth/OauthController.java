/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.security.oauth;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mytiki.account.features.latest.addr_reg.AddrRegService;
import com.mytiki.account.features.latest.api_key.ApiKeyService;
import com.mytiki.account.features.latest.app_info.AppInfoService;
import com.mytiki.account.features.latest.exchange.ExchangeService;
import com.mytiki.account.features.latest.otp.OtpService;
import com.mytiki.account.features.latest.refresh.RefreshService;
import com.mytiki.account.utilities.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.bind.annotation.*;

import static com.mytiki.account.utilities.Constants.TOKEN_EXPIRY_DURATION_SECONDS;

@XRayEnabled
@Tag(name = "")
@RestController
@RequestMapping(value = OauthController.ROUTE)
public class OauthController {
    public static final String ROUTE = Constants.API_LATEST_ROUTE + "oauth";
    public static final String REFRESH_COOKIE = "tiki-refresh-token";

    private final RefreshService refreshService;
    private final ApiKeyService apiKeyService;
    private final ExchangeService exchangeService;
    private final AddrRegService addrRegService;
    private final AppInfoService appInfoService;
    private final OtpService otpService;
    private final OauthScopes allowedScopes;
    private final OauthInternal oauthInternal;

    public OauthController(
            RefreshService refreshService,
            ApiKeyService apiKeyService,
            ExchangeService exchangeService,
            AddrRegService addrRegService,
            AppInfoService appInfoService,
            OtpService otpService,
            OauthScopes allowedScopes,
            OauthInternal oauthInternal) {
        this.refreshService = refreshService;
        this.apiKeyService = apiKeyService;
        this.exchangeService = exchangeService;
        this.addrRegService = addrRegService;
        this.appInfoService = appInfoService;
        this.otpService = otpService;
        this.allowedScopes = allowedScopes;
        this.oauthInternal = oauthInternal;
    }

    @Operation(hidden = true)
    @ApiResponse(responseCode = "200")
    @RequestMapping(
            method = RequestMethod.POST,
            path = "/revoke",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public void revoke(
            @RequestParam(required = false) String token,
            @RequestParam(required = false, name = "refresh_token") String refreshToken,
            @CookieValue(required = false, name = REFRESH_COOKIE) String refreshTokenCookie) {
        if(refreshTokenCookie != null) refreshService.revoke(refreshTokenCookie);
        if(refreshToken != null) refreshService.revoke(refreshToken);
        if(token != null) apiKeyService.revoke(token);
    }

    @Operation(hidden = true)
    @RequestMapping(
            method = RequestMethod.POST,
            path = "/token",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public OAuth2AccessTokenResponse grant(
            @RequestParam(name = "grant_type") AuthorizationGrantType grantType,
            @RequestParam(name = "client_id", required = false) String clientId,
            @RequestParam(name = "client_secret", required = false) String clientSecret,
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "password", required = false) String password,
            @RequestParam(name = "refresh_token", required = false) String refreshToken,
            @RequestParam(name = "subject_token", required = false) String subjectToken,
            @RequestParam(name = "subject_token_type", required = false) String subjectTokenType,
            @CookieValue(required = false, name = REFRESH_COOKIE) String refreshTokenCookie,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String expires,
            HttpServletResponse servletResponse) {
        OauthScopes scopes = allowedScopes.filter(scope);
        Long exp = expires == null || expires.isBlank() ? TOKEN_EXPIRY_DURATION_SECONDS : Long.parseLong(expires);
        OAuth2AccessTokenResponse token = switch (grantType.getValue()) {
            case "password" ->
                    otpService.authorize(username, password, scopes);
            case "client_credentials" -> {
                OauthSub sub = new OauthSub(clientId);
                yield switch (sub.getNamespace()){
                    case INTERNAL ->
                            oauthInternal.authorize(sub, clientSecret, scopes, exp);
                    case USER ->
                            apiKeyService.authorize(sub, clientSecret, scopes, exp);
//                    case APP -> {
//                        clienId is the appId
//                        clientSecret is the publishing Id
//                        appInfoService.authorize(scopes, clientId, clientSecret);
//                    }
                    case ADDRESS -> addrRegService.authorize(scopes, sub, clientSecret);
                    default -> throw new OAuth2AuthorizationException(
                            new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT));
                };
            }
            case "refresh_token" ->
                    refreshService.authorize(refreshTokenCookie != null ? refreshTokenCookie : refreshToken);
            case "urn:ietf:params:oauth:grant-type:token-exchange" ->
                    exchangeService.authorize(scopes, clientId, subjectToken, subjectTokenType);
            default -> throw new OAuth2AuthorizationException(
                    new OAuth2Error(OAuth2ErrorCodes.UNSUPPORTED_GRANT_TYPE));
        };
        if(token.getRefreshToken() != null) {
            Cookie cookie = new Cookie(REFRESH_COOKIE, token.getRefreshToken().getTokenValue());
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setMaxAge(Constants.REFRESH_EXPIRY_DURATION_SECONDS.intValue());
            servletResponse.addCookie(cookie);
        }
        return token;
    }
}
