package uk.gov.hmcts.cmc.claimstore.repositories;

import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.util.List;

public interface CaseSearchApi {
    List<Claim> getMediationClaims(String authorisation, LocalDate mediationAgreementDate);

    List<Claim> getClaimsPastIntentionToProceed(User user, LocalDate responseDate);
}
