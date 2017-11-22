package uk.gov.hmcts.cmc.claimstore.services;

public interface SealedClaimDocumentService {

    byte[] generateLegalDocument(final String claimExternalId, final String authorisation);

    byte[] generateCitizenDocument(final String claimExternalId, final String authorisation,
                                   final String submitterEmail);
}
