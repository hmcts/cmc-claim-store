package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import uk.gov.hmcts.cmc.claimstore.models.Address;

public class ClaimantContent extends PersonContent {

    public ClaimantContent(final String cliamantType,
                           final String fullName,
                           final Address address,
                           final Address correspondenceAddress,
                           final String email,
                           final String contactPerson,
                           final String businessName) {
        super(cliamantType, fullName, address, correspondenceAddress, email, contactPerson, businessName);
    }
}
