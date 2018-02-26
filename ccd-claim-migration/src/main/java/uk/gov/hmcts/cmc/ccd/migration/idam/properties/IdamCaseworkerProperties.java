package uk.gov.hmcts.cmc.ccd.migration.idam.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Component
@Validated
@ConfigurationProperties(prefix = "idam")
public class IdamCaseworkerProperties {

    @Valid
    private IdamCaseworker anonymousCaseworker;
    @Valid
    private IdamCaseworker systemUpdateUser;

    public IdamCaseworker getAnonymousCaseworker() {
        return anonymousCaseworker;
    }

    public void setAnonymousCaseworker(IdamCaseworker anonymousCaseworker) {
        this.anonymousCaseworker = anonymousCaseworker;
    }

    public IdamCaseworker getSystemUpdateUser() {
        return systemUpdateUser;
    }

    public void setSystemUpdateUser(IdamCaseworker systemUpdateUser) {
        this.systemUpdateUser = systemUpdateUser;
    }
}
