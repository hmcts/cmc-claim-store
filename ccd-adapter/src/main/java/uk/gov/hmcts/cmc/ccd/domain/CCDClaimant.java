package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class CCDClaimant {
    private CCDPartyType partyType;
    private String partyEmail;
    private CCDAddress partyServiceAddress;
    private String partyName;
    private String partyPhone;
    private CCDAddress partyAddress;
    private CCDAddress partyCorrespondenceAddress;
    private LocalDate partyDateOfBirth;
    private String partyContactPerson;
    private String partyCompaniesHouseNumber;
    private String partyTitle;
    private String partyBusinessName;
    private String representativeOrganisationName;
    private CCDAddress representativeOrganisationAddress;
    private String representativeOrganisationPhone;
    private String representativeOrganisationEmail;
    private String representativeOrganisationDxAddress;

}
