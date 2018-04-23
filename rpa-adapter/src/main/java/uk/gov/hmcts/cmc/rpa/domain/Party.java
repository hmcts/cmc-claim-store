package uk.gov.hmcts.cmc.rpa.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Party {
    private String type;
    private String fullName;
    private String title;
    private String businessName;
    private String companiesHouseNumber;
    private String contactPerson;
    private Address fullAddress;
    private String phoneNumber;
    private String emailAddress;
    private String dateOfBirth;
    private Address correspondenceAddress;
}
