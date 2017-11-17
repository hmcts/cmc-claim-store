package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Organisation {

    private final String name;
    private final CCDAddress address;
    private final CCDAddress correspondenceAddress;
    private final String mobilePhone;
    private final CCDRepresentative representative;
    private final String contactPerson;
    private final String companiesHouseNumber;
}
