package uk.gov.hmcts.cmc.claimstore.repositories;

import org.elasticsearch.index.query.QueryBuilder;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.util.List;

public interface CaseSearchApi {

    List<Claim> getMediationClaims(String authorisation, LocalDate mediationAgreementDate);

    List<Claim> getClaims(User user, QueryBuilder queryBuilder);

    List<Claim> getClaimsWithDefaultCCJ(User user, LocalDate ccjRequestedDate);

    List<Claim> getClaimsReadyForTransfer(User user);

    Integer getClaimCountForClaimant(String submitterId, User user);

    Integer getClaimCountForDefendant(String submitterId, User user);

    List<Claim> getClaimsForDefendant(String submitterId, User user, int index);

    List<Claim> getClaimsForClaimant(String submitterId, User user, int index);

}
