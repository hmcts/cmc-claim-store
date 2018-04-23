package uk.gov.hmcts.cmc.rpa.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Address {

    private String line1;
    private String line2;
    private String line3;
    private String city;
    private String postcode;
}
