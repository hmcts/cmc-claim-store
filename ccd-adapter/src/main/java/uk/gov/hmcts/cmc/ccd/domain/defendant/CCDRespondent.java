package uk.gov.hmcts.cmc.ccd.domain.defendant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentIntention;
import uk.gov.hmcts.cmc.ccd.domain.CCDTelephone;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDClaimantResponse;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDStatementOfMeans;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDMadeBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class CCDRespondent {
    private String partyName;
    private String letterHolderId;
    private String defendantId;
    private LocalDate responseDeadline;
    private LocalDate servedDate;

    private CCDParty claimantProvidedDetail;
    private String claimantProvidedPartyName;

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

    private CCDParty partyDetail;
    private LocalDateTime responseSubmittedOn;
    private CCDResponseType responseType;
    private String responseAmount;
    private String paymentDeclarationPaidAmount;
    private LocalDate paymentDeclarationPaidDate;
    private String paymentDeclarationExplanation;
    private List<CCDCollectionElement<CCDTimelineEvent>> defendantTimeLineEvents;
    private String defendantTimeLineComment;
    private List<CCDCollectionElement<CCDEvidenceRow>> responseEvidenceRows;
    private String responseEvidenceComment;
    private CCDDefenceType responseDefenceType;
    private String responseDefence;
    private CCDYesNoOption responseFreeMediationOption;
    private CCDTelephone responseMediationPhoneNumber;
    private String responseMediationContactPerson;
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

    private String redeterminationExplanation;
    private LocalDateTime redeterminationRequestedDate;
    private CCDMadeBy redeterminationMadeBy;
    private CCDDirectionsQuestionnaire directionsQuestionnaire;

    private String preferredCourtName;
    private CCDAddress preferredCourtAddress;
    private String preferredCourtReason;

    private String mediationFailedReason;
    private LocalDateTime mediationSettlementReachedAt;

    @JsonIgnore
    public boolean hasRepresentative() {
        return representativeOrganisationName != null
            || representativeOrganisationAddress != null
            || representativeOrganisationPhone != null
            || representativeOrganisationEmail != null
            || representativeOrganisationDxAddress != null;
    }

    @JsonIgnore
    public boolean hasPaymentDeclaration() {
        return paymentDeclarationExplanation != null
            || paymentDeclarationPaidAmount != null
            || paymentDeclarationPaidDate != null;
    }

    @JsonIgnore
    public boolean hasStatementOfTruth() {
        return responseDefendantSOTSignerName != null
            || responseDefendantSOTSignerRole != null;
    }
}
