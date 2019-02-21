package uk.gov.hmcts.cmc.ccd.domain.defendant;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentIntention;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDClaimantResponse;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDStatementOfMeans;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDMadeBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class CCDRespondent {
    private String letterHolderId;
    private String defendantId;
    private LocalDate responseDeadline;

    private CCDPartyType claimantProvidedType;
    private String claimantProvidedEmail;
    private CCDAddress claimantProvidedServiceAddress;
    private String claimantProvidedName;
    private CCDAddress claimantProvidedAddress;
    private CCDAddress claimantProvidedCorrespondenceAddress;
    private LocalDate claimantProvidedDateOfBirth;
    private String claimantProvidedContactPerson;
    private String claimantProvidedCompaniesHouseNumber;
    private String claimantProvidedTitle;
    private String claimantProvidedBusinessName;

    private String claimantProvidedRepresentativeOrganisationName;
    private CCDAddress claimantProvidedRepresentativeOrganisationAddress;
    private String claimantProvidedRepresentativeOrganisationPhone;
    private String claimantProvidedRepresentativeOrganisationEmail;
    private String claimantProvidedRepresentativeOrganisationDxAddress;

    private String representativeOrganisationName;
    private CCDAddress representativeOrganisationAddress;
    private String representativeOrganisationPhone;
    private String representativeOrganisationEmail;
    private String representativeOrganisationDxAddress;

    private CCDPartyType partyType;
    private String partyTitle;
    private String partyName;
    private LocalDate partyDateOfBirth;
    private String partyPhone;
    private String partyEmail;
    private CCDAddress partyAddress;
    private CCDAddress partyCorrespondenceAddress;
    private String partyBusinessName;
    private String partyContactPerson;
    private String partyCompaniesHouseNumber;

    private LocalDateTime responseSubmittedOn;
    private CCDResponseType responseType;
    private BigDecimal responseAmount;
    private LocalDate paymentDeclarationPaidDate;
    private String paymentDeclarationExplanation;
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

    private CCDPaymentIntention defendantPaymentIntention;
    private CCDStatementOfMeans statementOfMeans;

    private LocalDate paidInFullDate;
    private LocalDate directionsQuestionnaireDeadline;

    private List<CCDCollectionElement<CCDPartyStatement>> settlementPartyStatements;
    private LocalDateTime settlementReachedAt;

    private CCDCountyCourtJudgment countyCourtJudgmentRequest;
    private LocalDate ccjRequestedDate;

    private CCDClaimantResponse claimantResponse;

    private String reDeterminationExplanation;
    private LocalDateTime reDeterminationRequestedDate;
    private CCDMadeBy reDeterminationMadeBy;

}
