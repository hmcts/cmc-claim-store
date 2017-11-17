package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CCDIndividual {

    private final String title;
    private final String name;
    private final String mobilePhone;
    private final CCDAddress address;
    private final CCDAddress correspondenceAddress;
    private final String dateOfBirth;
    private final CCDRepresentative representative;

}
