package uk.gov.hmcts.cmc.ccd.deprecated.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDOrganisation {

    private String name;
    private CCDAddress address;
    private String email;
    private CCDAddress correspondenceAddress;
    private String phoneNumber;
    private CCDRepresentative representative;
    private String contactPerson;
    private String companiesHouseNumber;
}
