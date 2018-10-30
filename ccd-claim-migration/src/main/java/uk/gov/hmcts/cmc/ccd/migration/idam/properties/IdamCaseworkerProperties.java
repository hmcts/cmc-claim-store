package uk.gov.hmcts.cmc.ccd.migration.idam.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Component
@Validated
@ConfigurationProperties(prefix = "idam.caseworker")
public class IdamCaseworkerProperties {

    @Valid
    private IdamCaseworker anonymous;
    @Valid
    private IdamCaseworker system;

    public IdamCaseworker getAnonymous() {
        return anonymous;
    }

    public void setAnonymous(IdamCaseworker anonymous) {
        this.anonymous = anonymous;
    }

    public IdamCaseworker getSystem() {
        return system;
    }

    public void setSystem(IdamCaseworker system) {
        this.system = system;
    }
}

