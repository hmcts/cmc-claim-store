package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Data;

@Data
public class Organisation {

    private final String name;
    private final Address address;
    private final Address correspondenceAddress;
    private final String mobilePhone;
    private final Representative representative;
    private final String contactPerson;
    private final String companiesHouseNumber;
}
