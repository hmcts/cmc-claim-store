package uk.gov.hmcts.cmc.claimstore.services.user;

import uk.gov.hmcts.cmc.claimstore.models.idam.UserInfo;

public interface IUserInfoService {
    UserInfo getUserInfo(String bearerToken);
}
