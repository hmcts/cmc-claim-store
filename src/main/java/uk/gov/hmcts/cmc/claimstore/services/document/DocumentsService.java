package uk.gov.hmcts.cmc.claimstore.services.document;

public interface DocumentsService {
    byte[] generateClaimIssueReceipt(String externalId, String authorisation);

    byte[] getLegalSealedClaim(String externalId, String authorisation);

    byte[] generateDefendantResponseCopy(String externalId, String authorisation);

    byte[] generateDefendantResponseReceipt(String externalId, String authorisation);

    byte[] generateCountyCourtJudgement(String externalId, String authorisation);

    byte[] generateSettlementAgreement(String externalId, String authorisation);

}
