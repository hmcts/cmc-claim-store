package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.Address;

import java.util.Optional;

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
    private Address representativeOrganisationAddress;
    private String representativeOrganisationPhone;
    private String representativeOrganisationEmail;
    private String representativeOrganisationDxAddress;

}
