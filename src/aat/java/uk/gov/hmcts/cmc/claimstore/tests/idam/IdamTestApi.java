package uk.gov.hmcts.cmc.claimstore.tests.idam;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@SuppressWarnings("rawtypes")
@FeignClient(name = "idam-test-api", url = "${idam.api.url}/testing-support", decode404 = true)
public interface IdamTestApi {

    @RequestMapping(method = RequestMethod.POST, value = "/accounts")
    ResponseEntity createUser(CreateUserRequest createUserRequest);

    @RequestMapping(method = GET, value = "/accounts/pin/{letterHolderId}")
    ResponseEntity<String> getPinByLetterHolderId(@PathVariable("letterHolderId") String letterHolderId);

    @RequestMapping(method = RequestMethod.DELETE, value = "/accounts/{email}")
    ResponseEntity deleteUser(@PathVariable("email") String email);
}
