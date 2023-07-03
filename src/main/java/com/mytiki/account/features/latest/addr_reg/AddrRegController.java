package com.mytiki.account.features.latest.addr_reg;

import com.mytiki.account.security.oauth.OauthSub;
import com.mytiki.account.utilities.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "")
@RestController
@RequestMapping(value = AddrRegController.PATH_CONTROLLER)
public class AddrRegController {
    public static final String PATH_CONTROLLER = Constants.BASE_ROUTE + "app/{app-id}/address";

    private final AddrRegService service;

    public AddrRegController(AddrRegService service) {
        this.service = service;
    }


    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-addr-reg-post",
            summary = "Register Address",
            description = "Register an end user's address",
            security = @SecurityRequirement(name = "oauth", scopes = "account:public"))
    @Secured("SCOPE_account:public")
    @RequestMapping(method = RequestMethod.POST)
    public AddrRegAORsp post(
            JwtAuthenticationToken token,
            @PathVariable(name = "app-id") String appId,
            AddrRegAOReq body) {
        //OauthDecoder.guardGroups(token, appId);
        return service.register(appId, body);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-addr-reg-get",
            summary = "Get Address",
            description = "Retrieve the registration details for an end user address",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @Secured({"SCOPE_account:admin", "SCOPE_account:internal:read"})
    @RequestMapping(method = RequestMethod.GET, path = "/{address}")
    public AddrRegAORsp get(
            JwtAuthenticationToken token,
            @PathVariable(name = "app-id") String appId,
            @PathVariable(name = "address") String address) {
        //OauthDecoder.guardGroups(token, appId);
        return service.get(appId, address);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-addr-reg-get-all",
            summary = "Get Addresses",
            description = "Retrieve all registered end user addresses for an ID",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @Secured({"SCOPE_account:admin", "SCOPE_account:internal:read"})
    @RequestMapping(method = RequestMethod.GET)
    public List<AddrRegAORsp> getAll(
            JwtAuthenticationToken token,
            @PathVariable(name = "app-id") String appId,
            @RequestParam(name = "id") String id) {
        //OauthDecoder.guardGroups(token, appId);
        return service.getAll(appId, id);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-addr-reg-delete",
            summary = "Delete Address",
            description = "Delete a registered end user addresses",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.DELETE, path = "/{address}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(
            JwtAuthenticationToken token,
            @PathVariable(name = "app-id") String appId,
            @PathVariable(name = "address") String address) {
        //OauthDecoder.guardGroups(token, appId);
        OauthSub sub = new OauthSub(token.getName());
        //if app, app-id should match
        //if user, user should have app-id permissions

        service.delete(appId, address);
    }

    @Operation(operationId = Constants.PROJECT_DASH_PATH +  "-addr-reg-delete-all",
            summary = "Delete Addresses",
            description = "Delete all registered end user addresses for an ID",
            security = @SecurityRequirement(name = "oauth", scopes = "account:admin"))
    @Secured("SCOPE_account:admin")
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteAll(
            JwtAuthenticationToken token,
            @PathVariable(name = "app-id") String appId,
            @RequestParam(name = "id") String id) {
        //OauthDecoder.guardGroups(token, appId);
        service.deleteAll(appId, id);
    }
}
