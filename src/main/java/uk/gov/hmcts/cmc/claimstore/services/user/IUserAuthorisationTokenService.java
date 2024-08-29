package uk.gov.hmcts.cmc.claimstore.services.user;

import org.springframework.cache.annotation.Cacheable;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;

public interface IUserAuthorisationTokenService {
    @LogExecutionTime
    @Cacheable(value = "userOIDTokenCache")
    String getAuthorisationToken(String username, String password);
}
