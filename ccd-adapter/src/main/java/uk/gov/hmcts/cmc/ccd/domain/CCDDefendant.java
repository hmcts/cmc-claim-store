package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDDefenceType;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDPartyStatement;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDResponseType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class CCDDefendant {
    private String letterHolderId;

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
    private String partyTitle;
    private String partyName;
    private String partyDateOfBirth;
    private String partyPhone;
    private String partyEmail;
    private CCDAddress partyAddress;
    private CCDAddress partyCorrespondenceAddress;
    private String partyBusinessName;
    private String partyContactPerson;
    private String partyCompaniesHouseNumber;
    private CCDAddress partyServiceAddress;

    private String representativeOrganisationName;
    private CCDAddress representativeOrganisationAddress;
    private String representativeOrganisationPhone;
    private String representativeOrganisationEmail;
    private String representativeOrganisationDxAddress;

    private LocalDateTime responseSubmittedDateTime;
    private CCDResponseType responseType;
    private BigDecimal responseAmount;
    private LocalDate paymentDeclarationPaidDate;
    private String paymentDeclarationExplanation;
    private CCDPaymentIntention defendantPaymentIntention;
    private List<CCDCollectionElement<CCDTimelineEvent>> defendantTimeLineEvents;
    private String defendantTimeLineComment;
    private List<CCDCollectionElement<CCDEvidenceRow>> responseEvidenceRows;
    private String responseEvidenceComment;
    private CCDDefenceType responseDefenceType;
    private String responseDefence;
    private CCDYesNoOption responseFreeMediationOption;
    private CCDYesNoOption responseMoreTimeNeededOption;
    private String responseDefendantSOTSignerName;
    private String responseDefendantSOTSignerRole;
    private LocalDate paidInFullDate;

    private List<CCDCollectionElement<CCDPartyStatement>> settlementPartyStatements;
    private LocalDate settlementReachedAt;

    private CCJRequest countyCourtJudgement;
    private LocalDate ccjRequestedDate;
}
