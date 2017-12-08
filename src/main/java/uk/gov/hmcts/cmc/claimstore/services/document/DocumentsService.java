package uk.gov.hmcts.cmc.claimstore.services.document;

public interface DocumentsService {
    byte[] generateClaimIssueReceipt(String externalId, String submitterEmail);

    byte[] getLegalSealedClaim(String externalId, String authorisation);

    byte[] generateDefendantResponseCopy(String externalId);

    byte[] generateDefendantResponseReceipt(String externalId);

    byte[] generateCountyCourtJudgement(String externalId);

    byte[] generateSettlementAgreement(String externalId);

}
