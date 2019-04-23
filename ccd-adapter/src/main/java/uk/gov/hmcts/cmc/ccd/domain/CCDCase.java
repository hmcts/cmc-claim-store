package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingCourtType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingDurationType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirection;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType;

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
    private String externalId;
    private LocalDateTime submittedOn;
    private LocalDate issuedOn;
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
    private BigDecimal totalAmount;
    private CCDInterestType interestType;
    private BigDecimal interestBreakDownAmount;
    private String interestBreakDownExplanation;
    private BigDecimal interestRate;
    private String interestReason;
    private BigDecimal interestSpecificDailyAmount;
    private CCDInterestDateType interestDateType;
    private String currentInterestAmount;
    private LocalDate interestClaimStartDate;
    private String interestStartDateReason;
    private CCDInterestEndDateType interestEndDateType;
    private String paymentId;
    private BigDecimal paymentAmount;
    private String paymentReference;
    private String paymentStatus;
    private LocalDate paymentDateCreated;
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
    private String caseName;
    private LocalDate docUploadDeadline;
    private CCDDirectionPartyType docUploadForParty;
    private LocalDate eyewitnessUploadDeadline;
    private CCDDirectionPartyType eyewitnessUploadForParty;
    private List<CCDOrderDirectionType> directionList;
    private List<CCDOrderDirection> otherDirectionList;
    private CCDYesNoOption hearingIsRequired;
    private String newRequestedCourt;
    private String preferredCourtObjectingReason;
    private CCDHearingCourtType hearingCourt;
    private CCDHearingDurationType estimatedHearingDuration;
    private String hearingStatement;
}
