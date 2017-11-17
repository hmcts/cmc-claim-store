package uk.gov.hmcts.cmc.claimstore.services;

public interface SealedClaimDocumentService {

    byte[] generateLegalSealedClaim(final String claimExternalId, final String authorisation);

    byte[] generateCitizenSealedClaim(final String claimExternalId, final String authorisation,
                                      final String submitterEmail);
}
