package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDAddress {

    private String line1;
    private String line2;
    private String line3;
    private String city;
    private String postcode;
}
