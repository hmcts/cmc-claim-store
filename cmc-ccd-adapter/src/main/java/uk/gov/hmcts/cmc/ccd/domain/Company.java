package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Company {

    private final String name;
    private final Address address;
    private final String mobilePhone;
    private final String contactPerson;
    private final Address correspondenceAddress;
    private final Representative representative;
}
