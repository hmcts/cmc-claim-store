package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Data;

@Data
public class Company {

    private final String name;
    private final Address address;
    private final String mobilePhone;
    private final String contactPerson;
    private final Address correspondenceAddress;
    private final Representative representative;
}
