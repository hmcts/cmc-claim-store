package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDCompany {

    private String name;
    private CCDAddress address;
    private String mobilePhone;
    private String contactPerson;
    private CCDAddress correspondenceAddress;
    private CCDRepresentative representative;
}
