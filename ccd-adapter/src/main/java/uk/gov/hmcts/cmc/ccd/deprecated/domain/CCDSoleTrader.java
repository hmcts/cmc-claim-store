package uk.gov.hmcts.cmc.ccd.deprecated.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDSoleTrader {

    private String title;
    private String name;
    private String email;
    private String businessName;
    private String phoneNumber;
    private CCDAddress address;
    private CCDAddress correspondenceAddress;
    private CCDRepresentative representative;

}
