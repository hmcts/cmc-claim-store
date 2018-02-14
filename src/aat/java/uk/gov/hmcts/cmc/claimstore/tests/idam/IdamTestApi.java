package uk.gov.hmcts.cmc.claimstore.tests.idam;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "idam-test-api", url = "${idam.api.url}/testing-support")
public interface IdamTestApi {

    @RequestMapping(method = RequestMethod.POST, value = "/accounts")
    void createUser(CreateUserRequest createUserRequest);

}
