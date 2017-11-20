package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDIndividual {

    private String title;
    private String name;
    private String mobilePhone;
    private CCDAddress address;
    private CCDAddress correspondenceAddress;
    private String dateOfBirth;
    private CCDRepresentative representative;

}
