package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CCDParty {
    private CCDPartyType claimantProvidedType;
    private String claimantProvidedEmail;
    private CCDAddress claimantProvidedServiceAddress;
    private String claimantProvidedName;
    private String claimantProvidedPhoneNumber;
    private CCDAddress claimantProvidedAddress;
    private CCDAddress claimantProvidedCorrespondenceAddress;
    private String claimantProvidedDateOfBirth;
    private String claimantProvidedContactPerson;
    private String claimantProvidedCompaniesHouseNumber;
    private String claimantProvidedTitle;
    private String claimantProvidedBusinessName;
    private CCDPartyType partyType;
    private String partyEmail;
    private CCDAddress partyServiceAddress;
    private String partyName;
    private String partyPhoneNumber;
    private CCDAddress partyAddress;
    private CCDAddress partyCorrespondenceAddress;
    private String partyDateOfBirth;
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
