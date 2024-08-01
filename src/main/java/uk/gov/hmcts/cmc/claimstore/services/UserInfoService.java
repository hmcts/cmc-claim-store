package uk.gov.hmcts.cmc.claimstore.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserInfo;
import uk.gov.hmcts.cmc.claimstore.requests.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;

@Component
public class UserInfoService {

    private final IdamApi idamApi;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public UserInfoService(IdamApi idamApi) {
        this.idamApi = idamApi;
    }

    @LogExecutionTime
    @Cacheable(value = "userInfoCache")
    public UserInfo getUserInfo(String bearerToken) {
        logger.info("IDAM /o/userinfo invoked");
        return idamApi.retrieveUserInfo(bearerToken);
    }
}
