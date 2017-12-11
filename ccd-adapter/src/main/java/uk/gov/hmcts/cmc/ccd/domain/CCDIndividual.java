package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class CCDIndividual {

    private String title;
    private String name;
    private String email;
    private String mobilePhone;
    private CCDAddress address;
    private CCDAddress correspondenceAddress;
    private String dateOfBirth;
    private CCDRepresentative representative;

}
