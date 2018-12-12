package uk.gov.hmcts.cmc.ccd.deprecated.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDCompany {

    private String name;
    private CCDAddress address;
    private String email;
    private String phoneNumber;
    private String contactPerson;
    private CCDAddress correspondenceAddress;
    private CCDRepresentative representative;
}
