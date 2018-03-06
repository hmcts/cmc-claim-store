package uk.gov.hmcts.cmc.claimstore.tests.functional;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.idam.IdamTestService;

import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;

@Lazy
@Component
public class FunctionalTestsUsers {
    private final IdamTestService idamTestService;

    private User claimant;
    private AtomicInteger letterHolderIDCounter;

    public FunctionalTestsUsers(IdamTestService idamTestService) {
        this.idamTestService = idamTestService;
        this.letterHolderIDCounter = new AtomicInteger(1);
    }

    @PostConstruct
    public void initialize() {
        claimant = idamTestService.createCitizen();
    }

    public User getClaimant() {
        return claimant;
    }

    public User createDefendant() {
        String letterHolderId = String.valueOf(Integer.valueOf(claimant.getUserDetails().getId())
            + letterHolderIDCounter.getAndAdd(2));

        return idamTestService.createDefendant(letterHolderId);
    }
}
