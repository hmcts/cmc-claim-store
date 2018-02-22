package uk.gov.hmcts.cmc.claimstore.tests.functional;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.idam.IdamTestService;

import javax.annotation.PostConstruct;

@Lazy
@Component
public class FunctionalTestsUsers {

    private final IdamTestService idamTestService;

    private User claimant;

    public FunctionalTestsUsers(IdamTestService idamTestService) {
        this.idamTestService = idamTestService;
    }

    @PostConstruct
    public void initialize() {
        claimant = idamTestService.createCitizen();
    }

    public User getClaimant() {
        return claimant;
    }

}
