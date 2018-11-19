package uk.gov.hmcts.cmc.claimstore.idam;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.cmc.claimstore.idam.models.AuthenticateUserResponse;

@FeignClient(name = "idam-api", url = "${idam.api.url}")
public interface TacticalIdamApi {
    @RequestMapping(method = RequestMethod.POST, value = "/oauth2/authorize")
    AuthenticateUserResponse upliftUser(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestParam("upliftToken") String pinUserAuthorisation,
        @RequestParam("response_type") final String responseType,
        @RequestParam("client_id") final String clientId,
        @RequestParam("redirect_uri") final String redirectUri
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/oauth2/authorize",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    AuthenticateUserResponse authenticatePinUser(
        @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorisation,
        @RequestParam("response_type") final String responseType,
        @RequestParam("client_id") final String clientId,
        @RequestParam("redirect_uri") final String redirectUri
    );
}
