package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import uk.gov.hmcts.cmc.domain.models.Address;

public class ClaimantContent extends PersonContent {

    public ClaimantContent(
        String claimantType,
        String fullName,
        Address address,
        Address correspondenceAddress,
        String email,
        String contactPerson,
        String businessName,
        String phoneNumber,
        String dateOfBirth
    ) {
        super(claimantType, fullName, address, correspondenceAddress, email, contactPerson,
            businessName, phoneNumber, dateOfBirth);
    }
}
