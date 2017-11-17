package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CCDCompany {

    private final String name;
    private final CCDAddress address;
    private final String mobilePhone;
    private final String contactPerson;
    private final CCDAddress correspondenceAddress;
    private final CCDRepresentative representative;
}
