package uk.gov.hmcts.cmc.claimstore.functional.idam;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "idam-api", url = "${idam.api.url}")
public interface IdamTestApi {

    /**
     * Logs the user in.
     *
     * @param encodedCredentials Base64 encoded credentials to be sent in
     *                           <pre>Authorization: Basic &lt;base64 encoded data&gt;</pre> form
     * @return user's authorization token
     */
    @RequestMapping(method = RequestMethod.POST, value = "/oauth2/authorize")
    AccessToken logIn(@RequestHeader(HttpHeaders.AUTHORIZATION) String encodedCredentials);

}
