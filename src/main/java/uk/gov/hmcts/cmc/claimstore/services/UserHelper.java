package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;

@Component
public class UserHelper {
    
    public boolean isSolicitor(User user) {
        return user.getUserDetails().getRoles().stream().anyMatch("solicitor"::equals);
    }
}
