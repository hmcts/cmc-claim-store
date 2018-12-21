package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class CCDDefendant {
    private String letterHolderId;
    private String defendantId;
    private LocalDate responseDeadline;

    private CCDPartyType claimantProvidedType;
    private String claimantProvidedEmail;
    private CCDAddress claimantProvidedServiceAddress;
    private String claimantProvidedName;
    private String claimantProvidedPhone;
    private CCDAddress claimantProvidedAddress;
    private CCDAddress claimantProvidedCorrespondenceAddress;
    private LocalDate claimantProvidedDateOfBirth;
    private String claimantProvidedContactPerson;
    private String claimantProvidedCompaniesHouseNumber;
    private String claimantProvidedTitle;
    private String claimantProvidedBusinessName;

    private String representativeOrganisationName;
    private CCDAddress representativeOrganisationAddress;
    private String representativeOrganisationPhone;
    private String representativeOrganisationEmail;
    private String representativeOrganisationDxAddress;
}
