package uk.gov.hmcts.cmc.claimstore.idam;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinRequest;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.TokenExchangeResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserInfo;

@FeignClient(name = "idam-api", url = "${idam.api.url}")
public interface IdamApi {

    @RequestMapping(method = RequestMethod.POST, value = "/pin")
    GeneratePinResponse generatePin(
        GeneratePinRequest requestBody,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/o/token",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    TokenExchangeResponse authenticateUser(
        @RequestParam("client_id") String clientId,
        @RequestParam("client_secret") String clientSecret,
        @RequestParam("redirect_uri") String redirectUri,
        @RequestParam("grant_type") String grantType,
        @RequestParam("username") String username,
        @RequestParam("password") String password,
        @RequestParam("scope") String scope
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/oauth2/token",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    TokenExchangeResponse exchangeCodeLegacy(
        @RequestParam("code") String code,
        @RequestParam("grant_type") String grantType,
        @RequestParam("redirect_uri") String redirectUri,
        @RequestParam("client_id") String clientId,
        @RequestParam("client_secret") String clientSecret
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/o/token",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    TokenExchangeResponse exchangeCode(
        @RequestParam("code") String code,
        @RequestParam("grant_type") String grantType,
        @RequestParam("redirect_uri") String redirectUri,
        @RequestParam("client_id") String clientId,
        @RequestParam("client_secret") String clientSecret
    );

    @GetMapping("/o/userinfo")
    UserInfo retrieveUserInfo(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/oauth2/token",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    TokenExchangeResponse exchangeToken(
        @RequestParam("code") final String code,
        @RequestParam("grant_type") final String grantType,
        @RequestParam("redirect_uri") final String redirectUri,
        @RequestParam("client_id") final String clientId,
        @RequestParam("client_secret") final String clientSecret
    );

    @RequestMapping(method = RequestMethod.GET, value = "/details")
    UserDetails retrieveUserDetails(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation);
}
