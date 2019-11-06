package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderGenerationData;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
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
    private List<CCDCollectionElement<CCDClaimDocument>> caseDocuments;
    private List<CCDCollectionElement<CCDClaimDocument>> staffUploadedDocuments;
    private List<CCDCollectionElement<CCDScannedDocument>> scannedDocs;
    private String caseName;
    private CCDClaimSubmissionOperationIndicators claimSubmissionOperationIndicators;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String state;

    @JsonUnwrapped
    private CCDOrderGenerationData directionOrderData;
    private CCDDirectionOrder directionOrder;
    private CCDReviewOrder reviewOrder;
    private CCDChannelType channel;
    private LocalDate intentionToProceedDeadline;
}
