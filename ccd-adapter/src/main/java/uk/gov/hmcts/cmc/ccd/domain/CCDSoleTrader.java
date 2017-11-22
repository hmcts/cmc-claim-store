package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDSoleTrader {

    private String title;
    private String name;
    private String businessName;
    private String mobilePhone;
    private CCDAddress address;
    private CCDAddress correspondenceAddress;
    private CCDRepresentative representative;

}
