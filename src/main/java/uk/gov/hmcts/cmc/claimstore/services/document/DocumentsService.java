package uk.gov.hmcts.cmc.claimstore.services.document;

public interface DocumentsService {
    byte[] getLegalSealedClaim(String externalId, String authorisation);

    byte[] generateDefendantResponseCopy(String externalId);

    byte[] generateCountyCourtJudgement(String externalId);

    byte[] generateSettlementAgreement(String externalId);
}
