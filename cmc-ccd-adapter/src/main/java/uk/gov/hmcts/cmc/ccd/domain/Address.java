package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Data;

@Data
public class Address {

    private final String line1;
    private final String line2;
    private final String city;
    private final String postcode;
}
