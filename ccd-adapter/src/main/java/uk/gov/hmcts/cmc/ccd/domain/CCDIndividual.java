package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonIgnoreProperties(value = {"mobilePhone", "title"}) // Remove this after claim-store to CCD data migration is done
public class CCDIndividual {

    private String name;
    private String email;
    private String phoneNumber;
    private CCDAddress address;
    private CCDAddress correspondenceAddress;
    private String dateOfBirth;
    private CCDRepresentative representative;

}
