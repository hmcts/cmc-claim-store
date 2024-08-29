package uk.gov.hmcts.cmc.claimstore.services.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserInfo;
import uk.gov.hmcts.cmc.claimstore.requests.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;

@Component
@ConditionalOnProperty(prefix = "spring.user.token.cache", name = "enabled", havingValue = "true")
public class CacheableUserInfoService implements IUserInfoService {

    private final IdamApi idamApi;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public CacheableUserInfoService(IdamApi idamApi) {
        this.idamApi = idamApi;
    }

    @Override
    @LogExecutionTime
    @Cacheable(value = "userInfoCache")
    public UserInfo getUserInfo(String bearerToken) {
        logger.info("IDAM /o/userinfo invoked");
        return idamApi.retrieveUserInfo(bearerToken);
    }
}
