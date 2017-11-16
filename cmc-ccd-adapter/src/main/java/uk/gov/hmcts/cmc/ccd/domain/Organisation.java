package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Organisation {

    private final String name;
    private final Address address;
    private final Address correspondenceAddress;
    private final String mobilePhone;
    private final Representative representative;
    private final String contactPerson;
    private final String companiesHouseNumber;
}
