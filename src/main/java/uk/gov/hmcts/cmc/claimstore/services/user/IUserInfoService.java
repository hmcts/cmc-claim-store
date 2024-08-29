package uk.gov.hmcts.cmc.claimstore.services.user;

import org.springframework.cache.annotation.Cacheable;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserInfo;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;

public interface IUserInfoService {
    @LogExecutionTime
    @Cacheable(value = "userInfoCache")
    UserInfo getUserInfo(String bearerToken);
}
