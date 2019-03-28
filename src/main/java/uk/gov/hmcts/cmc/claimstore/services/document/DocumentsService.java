package uk.gov.hmcts.cmc.claimstore.services.document;

import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.Claim;

public interface DocumentsService {
    byte[] generateClaimIssueReceipt(String externalId, String authorisation);

    byte[] generateSealedClaim(String externalId, String authorisation);

    byte[] generateDefendantResponseReceipt(String externalId, String authorisation);

    byte[] generateCountyCourtJudgement(String externalId, String authorisation);

    byte[] generateSettlementAgreement(String externalId, String authorisation);

    Claim uploadToDocumentManagement(PDF document, String authorisation, Claim claim);

    void generateDefendantPinLetter(String externalId, String pin, String authorisation);
}
