package uk.gov.hmcts.cmc.claimstore.services.document;

public interface DocumentsService {
    byte[] generateClaimIssueReceipt(String externalId, String authorisation);

    byte[] generateSealedClaim(String externalId, String authorisation);

    byte[] generateDefendantResponseReceipt(String externalId, String authorisation);

    byte[] generateCountyCourtJudgement(String externalId, String authorisation);

    byte[] generateSettlementAgreement(String externalId, String authorisation);

    void generateDefendantPinLetter(String externalId, String pin, String authorisation);
}
