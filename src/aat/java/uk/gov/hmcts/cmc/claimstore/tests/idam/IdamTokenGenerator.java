package uk.gov.hmcts.cmc.claimstore.tests.idam;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.concurrent.TimeUnit;

@TestPropertySource("classpath:application.yaml")
@Service
public class IdamTokenGenerator {

    @Value("${idam.solicitor.username}")
    private static String solicitorUsername;

    @Value("${idam.solicitor.password}")
    private static String solicitorPassword;

    @Value("${idam.systemupdate.username}")
    private String systemUpdateUsername;

    @Value("${idam.systemupdate.password}")
    private String systemUpdatePassword;

    @Autowired
    private IdamClient idamClient;

    private final Cache<String, String> cache = Caffeine.newBuilder().expireAfterWrite(2, TimeUnit.HOURS).build();

    public  String generateIdamTokenForSolicitor(String solicitorUsername, String solicitorPassword) {
        String solicitorUserToken = cache.getIfPresent(solicitorUsername);
        if (solicitorUserToken == null) {
            solicitorUserToken = idamClient.getAccessToken(solicitorUsername, solicitorPassword);
            cache.put(solicitorUsername, solicitorUserToken);
        }
        return solicitorUserToken;
    }

    public String generateIdamTokenForCitizen(String citizenUsername, String citizenPassword) {
        String citizenUserToken = cache.getIfPresent(citizenUsername);
        if (citizenUserToken == null) {
            citizenUserToken = idamClient.getAccessToken(citizenUsername, citizenPassword);
            cache.put(citizenUsername, citizenUserToken);
        }
        return citizenUserToken;
    }

    public String generateIdamTokenForUpliftDefendant(String upliftDefendantUsername, String upliftDefendantPassword) {
        String upliftDefendantToken = cache.getIfPresent(upliftDefendantUsername);
        if (upliftDefendantToken == null) {
            upliftDefendantToken = idamClient.getAccessToken(upliftDefendantUsername, upliftDefendantPassword);
            cache.put(upliftDefendantUsername, upliftDefendantToken);
        }
        return upliftDefendantToken;
    }
}
