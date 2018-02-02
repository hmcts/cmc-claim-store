package uk.gov.hmcts.cmc.claimstore.aat.idam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class IdamTestService {

    private final IdamTestApi idamTestApi;

    @Autowired
    public IdamTestService(IdamTestApi idamTestApi) {
        this.idamTestApi = idamTestApi;
    }

    public String logIn(String userName, String password) {
        String encodedCredentials = Base64.getEncoder().encodeToString((userName + ":" + password).getBytes());
        return idamTestApi.logIn("Basic " + encodedCredentials).getAccessToken();
    }

}
