package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDClaimantResponse;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidence;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDSettlement;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDResponse;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CCDCase {

    private Long id;
    private String referenceNumber;
    private String submitterId;
    private String letterHolderId;
    private String defendantId;
    private String submittedOn;
    private String externalId;
    private String issuedOn;
    private LocalDate responseDeadline;
    private CCDYesNoOption moreTimeRequested;
    private String submitterEmail;
    private String reason;
    private String feeCode;
    private String feeAccountNumber;
    private BigInteger feeAmountInPennies;
    private String externalReferenceNumber;
    private AmountType amountType;
    private BigDecimal amountLowerValue;
    private BigDecimal amountHigherValue;
    private List<CCDCollectionElement<CCDAmountRow>> amountBreakDown;
    private CCDNotKnown notKnown;
    private CCDInterestType interestType;
    private BigDecimal interestBreakDownAmount;
    private String interestBreakDownExplanation;
    private BigDecimal interestRate;
    private String interestReason;
    private BigDecimal interestSpecificDailyAmount;
    private CCDInterestDateType interestDateType;
    private LocalDate interestClaimStartDate;
    private String interestStartDateReason;
    private CCDInterestEndDateType interestEndDateType;
    private String paymentId;
    private BigDecimal paymentAmount;
    private String paymentReference;
    private String paymentDateCreated;
    private String paymentStatus;
    private String preferredCourt;
    private String personalInjuryGeneralDamages;
    private String housingDisrepairCostOfRepairDamages;
    private String housingDisrepairOtherDamages;
    private String sotSignerName;
    private String sotSignerRole;
    private List<CCDCollectionElement<CCDParty>> claimants;
    private List<CCDCollectionElement<CCDParty>> defendants;
    private List<CCDCollectionElement<CCDTimelineEvent>> timeline;
    private List<CCDCollectionElement<CCDEvidenceRow>> evidence;

    private CCDCountyCourtJudgment countyCourtJudgment;
    private LocalDateTime countyCourtJudgmentRequestedAt;
    private String defendantEmail;
    private CCDResponse response;
    private LocalDateTime respondedAt;
    private CCDSettlement settlement;
    private LocalDateTime settlementReachedAt;
    private CCDDocument sealedClaimDocument;
    private String features;
    private LocalDate moneyReceivedOn;
    private CCDClaimantResponse claimantResponse;
    private LocalDateTime claimantRespondedAt;
    private LocalDate directionsQuestionnaireDeadline;

}
