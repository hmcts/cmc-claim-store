package uk.gov.hmcts.cmc.claimstore.services.document;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

public interface DocumentsService {
    byte[] generateClaimIssueReceipt(String externalId, String authorisation);

    byte[] getSealedClaim(String externalId, String authorisation);

    byte[] generateDefendantResponseReceipt(String externalId, String authorisation);

    byte[] generateCountyCourtJudgement(String externalId, String authorisation);

    byte[] generateSettlementAgreement(String externalId, String authorisation);

    byte[] uploadToDocumentManagement(byte[] documentBytes,
        String authorisation,
        String baseFileName,
        ClaimDocumentType claimDocumentType,
        Claim claim);
}
