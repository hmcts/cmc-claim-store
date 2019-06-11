package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDApplicant {
    private CCDParty partyDetail;
    private String partyName;
    private String representativeOrganisationName;
    private CCDAddress representativeOrganisationAddress;
    private String representativeOrganisationPhone;
    private String representativeOrganisationEmail;
    private String representativeOrganisationDxAddress;
    private CCDYesNoOption leadApplicantIndicator;
    private String preferredCourtName;
    private CCDAddress preferredCourtAddress;
    private String preferredCourtReason;
}
