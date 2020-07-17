package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseType;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingDurationType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirection;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CCDCase {

    private Long id;
    private String previousServiceCaseReference;
    private String submitterId;
    private String externalId;
    private LocalDateTime submittedOn;
    private LocalDate issuedOn;
    private String submitterEmail;
    private String reason;
    private String feeCode;
    private String feeAccountNumber;
    private String feeAmountInPennies;
    private String externalReferenceNumber;
    private AmountType amountType;
    private String amountLowerValue;
    private String amountHigherValue;
    private List<CCDCollectionElement<CCDAmountRow>> amountBreakDown;
    private String totalAmount;
    private CCDInterestType interestType;
    private String interestBreakDownAmount;
    private String interestBreakDownExplanation;
    private BigDecimal interestRate;
    private String interestReason;
    private String interestSpecificDailyAmount;
    private CCDInterestDateType interestDateType;
    private String currentInterestAmount;
    private LocalDate interestClaimStartDate;
    private String interestStartDateReason;
    private CCDInterestEndDateType interestEndDateType;
    private String paymentId;
    private String paymentAmount;
    private String paymentReference;
    private String paymentStatus;
    private LocalDate paymentDateCreated;
    private String paymentNextUrl;
    private String paymentReturnUrl;
    private String paymentTransactionId;
    private String paymentFeeId;
    private String preferredCourt;
    private String personalInjuryGeneralDamages;
    private String housingDisrepairCostOfRepairDamages;
    private String housingDisrepairOtherDamages;
    private String sotSignerName;
    private String sotSignerRole;
    private List<CCDCollectionElement<CCDApplicant>> applicants;
    private List<CCDCollectionElement<CCDRespondent>> respondents;
    private List<CCDCollectionElement<CCDTimelineEvent>> timeline;
    private List<CCDCollectionElement<CCDEvidenceRow>> evidence;
    private String features;
    private CCDYesNoOption migratedFromClaimStore;
    @Builder.Default
    private List<CCDCollectionElement<CCDClaimDocument>> caseDocuments = Collections.emptyList();
    @Builder.Default
    private List<CCDCollectionElement<CCDScannedDocument>> scannedDocuments = Collections.emptyList();
    private String ocon9xForm;
    private List<CCDCollectionElement<CCDClaimDocument>> staffUploadedDocuments;
    private String caseName;
    private CCDClaimSubmissionOperationIndicators claimSubmissionOperationIndicators;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String state;
    private CCDYesNoOption evidenceHandled;

    private LocalDate docUploadDeadline;

    private CCDDirectionPartyType docUploadForParty;

    private LocalDate eyewitnessUploadDeadline;

    private CCDDirectionPartyType eyewitnessUploadForParty;

    @Builder.Default
    private List<CCDOrderDirectionType> directionList = Collections.emptyList();

    @Builder.Default
    private List<CCDCollectionElement<CCDOrderDirection>> otherDirections = Collections.emptyList();

    @Builder.Default
    private List<CCDCollectionElement<String>> extraDocUploadList = Collections.emptyList();

    private CCDYesNoOption paperDetermination;

    private String newRequestedCourt;

    private String preferredDQCourt;

    private String preferredCourtObjectingParty;
    private String preferredCourtObjectingReason;

    private String hearingCourt;

    private String hearingCourtName;

    private CCDAddress hearingCourtAddress;

    private CCDHearingDurationType estimatedHearingDuration;

    private CCDDocument draftOrderDoc;
    private CCDDocument draftLetterDoc;

    private CCDYesNoOption expertReportPermissionPartyAskedByClaimant;
    private CCDYesNoOption expertReportPermissionPartyAskedByDefendant;
    private CCDYesNoOption grantExpertReportPermission;
    //TODO - Remove once CCD 1.5.9 released
    private CCDYesNoOption expertReportPermissionPartyGivenToClaimant;
    private CCDYesNoOption expertReportPermissionPartyGivenToDefendant;
    @Builder.Default
    private List<CCDCollectionElement<String>> expertReportInstructionClaimant = Collections.emptyList();
    @Builder.Default
    private List<CCDCollectionElement<String>> expertReportInstructionDefendant = Collections.emptyList();

    private String expertReportInstruction;

    private CCDDirectionOrder directionOrder;
    private CCDReviewOrder reviewOrder;
    private CCDChannelType channel;
    private LocalDate intentionToProceedDeadline;
    private LocalDateTime dateReferredForDirections;
    private GeneralLetterContent generalLetterContent;
    private CCDLaList assignedTo;
    private CCDContactPartyType contactChangeParty;
    private CCDContactChangeContent contactChangeContent;
    private CCDProceedOnPaperReasonType proceedOnPaperReason;
    private String proceedOnPaperOtherReason;
    private LocalDate calculatedResponseDeadline;
    private CCDResponseType paperAdmissionType;
    private CCDTransferContent transferContent;
    /**
     * Temporary variables that are not to be persisted to case data but are only used during events.
     */
    @Builder.Default
    private List<CCDCollectionElement<CCDScannedDocument>> temporaryScannedDocuments = Collections.emptyList();
    private LocalDate dateOfHandoff;
}
