package uk.gov.hmcts.cmc.claimstore.tests.functional;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.idam.IdamTestService;

@Lazy
@Component
public class FunctionalTestsUsers {
    private final IdamTestService idamTestService;

    public FunctionalTestsUsers(IdamTestService idamTestService) {
        this.idamTestService = idamTestService;
    }

    public User createDefendant(final String hackHackHackClaimantId) {
        // HACK no way to know the letter holder ID currently so we pray claimantId + 1 works for now
        String letterHolderId = String.valueOf(Integer.valueOf(hackHackHackClaimantId) + 1);

        return idamTestService.createDefendant(letterHolderId);
    }
}
