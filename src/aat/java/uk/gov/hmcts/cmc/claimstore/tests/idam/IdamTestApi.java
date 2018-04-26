package uk.gov.hmcts.cmc.claimstore.tests.idam;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@FeignClient(name = "idam-test-api", url = "${idam.api.url}/testing-support")
public interface IdamTestApi {

    @RequestMapping(method = RequestMethod.POST, value = "/accounts")
    void createUser(CreateUserRequest createUserRequest);

    @RequestMapping(value = "/accounts/pin/{letterHolderId}", method = GET)
    ResponseEntity<String> getPinByLetterHolderId(@PathVariable("letterHolderId") String letterHolderId);
}
