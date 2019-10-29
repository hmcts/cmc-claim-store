package uk.gov.hmcts.cmc.claimstore.services.staff.models;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.Address;

@Getter
@Builder
public class PersonContent {
    private final String partyType;
    private final String fullName;
    private final Address address;
    private final Address correspondenceAddress;
    private final String email;
    private final String contactPerson;
    private final String businessName;
    private final String phoneNumber;
    private final String dateOfBirth;
}
