package uk.gov.hmcts.cmc.claimstore.idam;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.cmc.claimstore.idam.models.ActivationData;
import uk.gov.hmcts.cmc.claimstore.idam.models.AuthenticateUserResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinRequest;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "idam-api", url = "${idam.api.url}")
public interface IdamApi {
    @RequestMapping(method = RequestMethod.GET, value = "/details")
    UserDetails retrieveUserDetails(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation);

    @RequestMapping(method = RequestMethod.POST, value = "/pin")
    GeneratePinResponse generatePin(
        GeneratePinRequest requestBody,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation
    );

    @RequestMapping(method = RequestMethod.POST, value = "/oauth2/authorize")
    AuthenticateUserResponse authenticateUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation);


    @RequestMapping(value = "/register-self", method = RequestMethod.POST)
    ResponseEntity<?> register(@RequestBody final UserDetails user,
                               @RequestHeader(value = AUTHORIZATION, required = true) final String authHeaderValue);

    @RequestMapping(value = "/activate", method = RequestMethod.PUT)
    public ResponseEntity<?> activate(@RequestBody ActivationData data,
                                      @RequestHeader(value = AUTHORIZATION) final String authHeader);
}
