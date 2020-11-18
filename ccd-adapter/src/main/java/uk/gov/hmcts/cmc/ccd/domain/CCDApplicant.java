package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CCDApplicant {
    private CCDParty partyDetail;
    private String partyName;
    private String representativeOrganisationName;
    private CCDAddress representativeOrganisationAddress;
    private String representativeOrganisationPhone;
    private String representativeOrganisationEmail;
    private String representativeOrganisationDxAddress;
    private CCDYesNoOption leadApplicantIndicator;
    private String pcqId;

    @JsonIgnore
    public boolean hasRepresentative() {
        return representativeOrganisationName != null
            || representativeOrganisationPhone != null
            || representativeOrganisationAddress != null
            || representativeOrganisationEmail != null
            || representativeOrganisationDxAddress != null;
    }
}
