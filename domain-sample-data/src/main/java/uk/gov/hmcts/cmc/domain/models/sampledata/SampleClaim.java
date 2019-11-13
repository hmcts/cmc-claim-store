package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.ChannelType;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.orders.DirectionOrder;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleSettlement;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CCJ_REQUEST;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType.DEFAULT;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.IMMEDIATELY;
import static uk.gov.hmcts.cmc.domain.models.offers.MadeBy.CLAIMANT;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest.standardInterestBuilder;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.ISSUE_DATE;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.NOW_IN_LOCAL_ZONE;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.RESPONSE_DEADLINE;

public final class SampleClaim {

    public static final String USER_ID = "1";
    public static final String LETTER_HOLDER_ID = "2";
    public static final String DEFENDANT_ID = "4";
    public static final Long CLAIM_ID = 3L;
    public static final String REFERENCE_NUMBER = "000CM001";
    public static final UUID RAND_UUID = UUID.randomUUID();
    public static final String EXTERNAL_ID = RAND_UUID.toString();
    public static final boolean NOT_REQUESTED_FOR_MORE_TIME = false;
    public static final LocalDateTime NOT_RESPONDED = null;
    public static final String SUBMITTER_EMAIL = "claimant@mail.com";
    public static final String DEFENDANT_EMAIL = SampleTheirDetails.DEFENDANT_EMAIL;
    public static final String DEFENDANT_EMAIL_VERIFIED = "defendant@mail.com";
    private static final URI DOCUMENT_URI = URI.create("http://localhost/doc.pdf");
    private static final String OCMC = "OCMC";

    private String submitterId = USER_ID;
    private String letterHolderId = LETTER_HOLDER_ID;
    private String defendantId = DEFENDANT_ID;
    private Long claimId = CLAIM_ID;
    private String referenceNumber = REFERENCE_NUMBER;
    private String externalId = EXTERNAL_ID;
    private boolean isMoreTimeRequested = NOT_REQUESTED_FOR_MORE_TIME;
    private LocalDate responseDeadline = RESPONSE_DEADLINE;
    private String submitterEmail = SUBMITTER_EMAIL;
    private LocalDateTime createdAt = NOW_IN_LOCAL_ZONE;
    private LocalDateTime respondedAt = NOT_RESPONDED;
    private LocalDate issuedOn = ISSUE_DATE;
    private CountyCourtJudgment countyCourtJudgment = null;
    private LocalDateTime countyCourtJudgmentRequestedAt = null;
    private ClaimData claimData = SampleClaimData.builder().withExternalId(RAND_UUID).build();
    private Response response;
    private String defendantEmail;
    private Settlement settlement = null;
    private LocalDateTime settlementReachedAt = null;
    private List<String> features = Collections.singletonList("admissions");
    private LocalDateTime claimantRespondedAt;
    private ClaimantResponse claimantResponse;
    private LocalDate directionsQuestionnaireDeadline;
    private LocalDate moneyReceivedOn;
    private LocalDateTime reDeterminationRequestedAt;
    private ReDetermination reDetermination = new ReDetermination("I feel defendant can pay", CLAIMANT);
    private ClaimDocumentCollection claimDocumentCollection = new ClaimDocumentCollection();
    private LocalDate claimantResponseDeadline;
    private ClaimState state = ClaimState.OPEN;
    private ClaimSubmissionOperationIndicators claimSubmissionOperationIndicators
        = ClaimSubmissionOperationIndicators.builder().build();
    private Long ccdCaseId = 1023467890123456L;
    private ReviewOrder reviewOrder;
    private DirectionOrder directionOrder;
    private ChannelType channel;
    private LocalDate intentionToProceedDeadline = NOW_IN_LOCAL_ZONE.toLocalDate().plusDays(33);

    private SampleClaim() {
    }

    public static Claim getDefault() {
        return builder()
            .withClaimData(SampleClaimData.submittedByClaimantBuilder().withExternalId(RAND_UUID).build())
            .withCountyCourtJudgment(
                SampleCountyCourtJudgment.builder()
                    .ccjType(CountyCourtJudgmentType.ADMISSIONS)
                    .paymentOption(IMMEDIATELY)
                    .build()
            ).withResponse(SampleResponse.FullDefence
                .builder()
                .withDefenceType(DefenceType.DISPUTE)
                .withMediation(YES)
                .build()
            ).withState(ClaimState.OPEN)
            .build();
    }

    public static Claim getWithClaimSubmissionOperationIndicators() {
        return builder()
            .withClaimData(SampleClaimData.submittedByClaimantBuilder().withExternalId(RAND_UUID).build())
            .withCountyCourtJudgment(
                SampleCountyCourtJudgment.builder()
                    .ccjType(CountyCourtJudgmentType.ADMISSIONS)
                    .paymentOption(IMMEDIATELY)
                    .build()
            ).withResponse(SampleResponse.FullDefence
                .builder()
                .withDefenceType(DefenceType.DISPUTE)
                .withMediation(YES)
                .build()
            )
            .withClaimSubmissionOperationIndicators(
                ClaimSubmissionOperationIndicators.builder()
                    .bulkPrint(YES)
                    .claimantNotification(YES)
                    .claimIssueReceiptUpload(YES)
                    .defendantNotification(YES)
                    .rpa(YES)
                    .sealedClaimUpload(YES)
                    .staffNotification(YES)
                    .build()
            )
            .build();
    }

    public static Claim withFullClaimData() {
        return builder()
            .withClaimData(SampleClaimData.builder()
                .withExternalId(RAND_UUID)
                .withInterest(new SampleInterest()
                    .withType(Interest.InterestType.BREAKDOWN)
                    .withInterestBreakdown(SampleInterestBreakdown.validDefaults())
                    .withRate(BigDecimal.valueOf(8))
                    .withReason("Need flat rate").build())
                .withPayment(SamplePayment.builder().build())
                .build())
            .build();
    }

    public static Claim getClaimWithFullDefenceNoMediation() {
        return builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withCountyCourtJudgment(
                SampleCountyCourtJudgment.builder()
                    .paymentOption(IMMEDIATELY)
                    .build()
            ).withResponse(SampleResponse.FullDefence
                .builder()
                .withDefenceType(DefenceType.DISPUTE)
                .withMediation(NO)
                .withMoreTimeNeededOption(NO)
                .build()
            )
            .withRespondedAt(LocalDateTime.now())
            .withDirectionsQuestionnaireDeadline(LocalDate.now())
            .build();
    }

    public static Claim getClaimWithPartAdmissionAndNoMediation() {
        return builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withCountyCourtJudgment(
                SampleCountyCourtJudgment.builder()
                    .paymentOption(IMMEDIATELY)
                    .build()
            ).withResponse(SampleResponse.PartAdmission.builder()
                .buildWithDirectionsQuestionnaireWitNoMediation()
            )
            .withRespondedAt(LocalDateTime.now())
            .withDirectionsQuestionnaireDeadline(LocalDate.now())
            .build();
    }

    public static Claim getClaimWithFullDefenceWithMediation() {
        return builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withResponse(SampleResponse.FullAdmission.builder()
                .buildWithFreeMediation()
            )
            .withRespondedAt(LocalDateTime.now())
            .withDirectionsQuestionnaireDeadline(LocalDate.now())
            .build();
    }

    public static Claim getClaimWithFullDefenceAlreadyPaid() {
        return builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withCountyCourtJudgment(
                SampleCountyCourtJudgment.builder()
                    .paymentOption(IMMEDIATELY)
                    .build()
            ).withResponse(SampleResponse.FullDefence
                .builder()
                .withDefenceType(DefenceType.ALREADY_PAID)
                .build()
            ).withRespondedAt(LocalDateTime.now())
            .build();
    }

    public static Claim getWithDefaultResponse() {
        return getWithResponse(SampleResponse.validDefaults());
    }

    public static Claim getWithResponse(Response response) {
        return builder()
            .withClaimData(SampleClaimData.validDefaults())
            .withResponse(response)
            .withRespondedAt(LocalDateTime.now())
            .withDefendantEmail(DEFENDANT_EMAIL)
            .build();
    }

    public static Claim getWithResponseDefendantEmailVerified(Response response) {
        return builder()
            .withClaimData(SampleClaimData.validDefaults())
            .withResponse(response)
            .withRespondedAt(LocalDateTime.now())
            .withDefendantEmail(DEFENDANT_EMAIL_VERIFIED)
            .build();
    }

    public static Claim withNoResponse() {
        return builder()
            .withClaimData(SampleClaimData.validDefaults())
            .build();
    }

    public static Claim withDefaultCountyCourtJudgment() {
        return builder().withDefendantEmail(DEFENDANT_EMAIL)
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withCountyCourtJudgment(
                SampleCountyCourtJudgment.builder()
                    .paymentOption(IMMEDIATELY)
                    .ccjType(DEFAULT)
                    .build()
            ).withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();
    }

    public static Claim getWithClaimantResponse() {
        return builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withResponse(SampleResponse.FullAdmission.validDefaults())
            .withRespondedAt(LocalDateTime.now())
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withClaimantRespondedAt(LocalDateTime.now())
            .withClaimantResponse(SampleClaimantResponse.validDefaultAcceptation())
            .build();
    }

    public static Claim getWithClaimantResponse(ClaimantResponse claimantResponse) {
        return builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withResponse(SampleResponse.FullAdmission.validDefaults())
            .withRespondedAt(LocalDateTime.now())
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withClaimantRespondedAt(LocalDateTime.now())
            .withClaimantResponse(claimantResponse)
            .build();
    }

    public static Claim getWithClaimantResponseRejectionForPartAdmissionAndMediation() {
        return builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withResponse(
                SampleResponse
                    .PartAdmission
                    .builder()
                    .buildWithFreeMediation())
            .withRespondedAt(LocalDateTime.now())
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withClaimantRespondedAt(LocalDateTime.now())
            .withClaimantResponse(SampleClaimantResponse
                .ClaimantResponseRejection
                .builder()
                .buildRejectionWithFreeMediation())
            .build();
    }

    public static Claim getWithClaimantResponseRejectionForPartAdmissionNoMediation() {
        return builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withResponse(
                SampleResponse
                    .PartAdmission
                    .builder()
                    .buildWithFreeMediation())
            .withRespondedAt(LocalDateTime.now())
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withClaimantRespondedAt(LocalDateTime.now())
            .withClaimantResponse(SampleClaimantResponse.validDefaultRejection())
            .build();
    }

    public static Claim getDefaultForLegal() {
        return builder()
            .withReferenceNumber("012LR345")
            .withClaimData(SampleClaimData.builder().withPayment(null).build())
            .build();
    }

    public static Claim getCitizenClaim() {
        return builder()
            .withClaimData(SampleClaimData.submittedByClaimantBuilder().withExternalId(RAND_UUID).build())
            .build();
    }

    public static Claim getLegalDataWithReps() {
        return builder()
            .withClaimData(SampleClaimData.builder()
                .withExternalId(RAND_UUID)
                .withAmount(SampleAmountRange.builder().build())
                .clearDefendants()
                .withDefendant(SampleTheirDetails.builder()
                    .withRepresentative(SampleRepresentative.builder().build())
                    .individualDetails())
                .build()
            )
            .build();
    }

    public static Claim getClaimWithSealedClaimLink(URI sealedClaimUri) {
        return builder().withSealedClaimDocument(sealedClaimUri).build();
    }

    public static Claim claim(ClaimData claimData, String referenceNumber) {
        return Claim.builder()
            .id(CLAIM_ID)
            .submitterId(USER_ID)
            .letterHolderId(LETTER_HOLDER_ID)
            .defendantId(DEFENDANT_ID)
            .externalId(EXTERNAL_ID)
            .referenceNumber(referenceNumber)
            .claimData(Optional.ofNullable(claimData).orElse(SampleClaimData.submittedByClaimant()))
            .createdAt(NOW_IN_LOCAL_ZONE)
            .issuedOn(ISSUE_DATE)
            .responseDeadline(RESPONSE_DEADLINE)
            .moreTimeRequested(NOT_REQUESTED_FOR_MORE_TIME)
            .respondedAt(LocalDateTime.now())
            .submitterEmail(SUBMITTER_EMAIL)
            .build();
    }

    public static Claim getWithSubmissionInterestDate() {
        return builder()
            .withClaimData(
                SampleClaimData
                    .builder()
                    .withInterest(
                        standardInterestBuilder()
                            .withInterestDate(SampleInterestDate.submission())
                            .build())
                    .build())
            .build();
    }

    public static Claim getWithResponseDeadline(LocalDate responseDeadline) {
        return builder().withResponseDeadline(responseDeadline).build();
    }

    public static Claim getWithSettlement(Settlement settlement) {
        return builder().withSettlement(settlement)
            .withSettlementReachedAt(LocalDateTime.now())
            .build();
    }

    public static Claim getClaimWithNoDefendantEmail() {

        return SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withDefendant(SampleTheirDetails.builder().withPhone(null).withEmail(null).individualDetails())
                    .build()
            ).build();
    }

    public static Claim getClaimFullDefenceStatesPaidWithAcceptation() {
        return builder()
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withResponse(
                SampleResponse.FullDefence
                    .builder()
                    .withDefenceType(DefenceType.ALREADY_PAID)
                    .withMediation(NO)
                    .build())
            .withRespondedAt(LocalDateTime.now())
            .withClaimantResponse(SampleClaimantResponse.validDefaultAcceptation())
            .withCountyCourtJudgment(
                SampleCountyCourtJudgment.builder()
                    .paymentOption(IMMEDIATELY)
                    .ccjType(DEFAULT)
                    .build()
            ).withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();
    }

    public static Claim getClaimWithSettlementAgreementRejected() {

        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.builder().build(), CLAIMANT, null);
        settlement.acceptCourtDetermination(CLAIMANT, null);
        settlement.reject(MadeBy.DEFENDANT, null);

        return builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withResponse(SampleResponse.FullAdmission.validDefaults())
            .withSettlement(settlement)
            .build();
    }

    public static Claim withSettlementReached() {

        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.builder().build(), CLAIMANT, null);
        settlement.acceptCourtDetermination(CLAIMANT, null);
        settlement.countersign(MadeBy.DEFENDANT, null);

        return builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withResponse(SampleResponse.FullAdmission.validDefaults())
            .withSettlement(settlement)
            .build();
    }

    public static Claim getWithSealedClaimDocument() {
        return builder()
            .withSealedClaimDocument(DOCUMENT_URI)
            .build();
    }

    public static Claim getWithClaimIssueReceiptDocument() {
        return builder()
            .withClaimIssueReceiptDocument(DOCUMENT_URI)
            .build();
    }

    public static Claim getWithDefendantResponseReceiptDocument() {
        return builder().withResponse(
            SampleResponse.validDefaults())
            .withDefendantResponseReceiptDocument(DOCUMENT_URI)
            .build();
    }

    public static Claim getWithCCJRequestDocument() {
        return builder().withCountyCourtJudgment(
            SampleCountyCourtJudgment.builder().ccjType(DEFAULT).build())
            .withCountyCourtJudgmentRequestedAt(LocalDateTimeFactory.nowInLocalZone())
            .withCCJRequestDocument(DOCUMENT_URI)
            .build();
    }

    public static Claim getWithSettlementAgreementDocument() {
        return builder().withSettlement(
            SampleSettlement.validDefaults())
            .withSettlementAgreementDocument(DOCUMENT_URI)
            .build();
    }

    public static SampleClaim builder() {
        return new SampleClaim();
    }

    public Claim build() {
        return new Claim(
            claimId,
            submitterId,
            letterHolderId,
            defendantId,
            externalId,
            referenceNumber,
            claimData,
            createdAt,
            issuedOn,
            issuedOn.plusDays(5),
            responseDeadline,
            isMoreTimeRequested,
            submitterEmail,
            respondedAt,
            response,
            defendantEmail,
            countyCourtJudgment,
            countyCourtJudgmentRequestedAt,
            settlement,
            settlementReachedAt,
            features,
            claimantRespondedAt,
            claimantResponse,
            directionsQuestionnaireDeadline,
            moneyReceivedOn,
            reDetermination,
            reDeterminationRequestedAt,
            claimDocumentCollection,
            claimantResponseDeadline,
            state,
            claimSubmissionOperationIndicators,
            ccdCaseId,
            reviewOrder,
            directionOrder,
            channel,
            intentionToProceedDeadline
        );
    }

    public SampleClaim withSubmitterId(String userId) {
        this.submitterId = userId;
        return this;
    }

    public SampleClaim withLetterHolderId(String letterHolderId) {
        this.letterHolderId = letterHolderId;
        return this;
    }

    public SampleClaim withDefendantId(String defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public SampleClaim withClaimId(Long claimId) {
        this.claimId = claimId;
        return this;
    }

    public SampleClaim withReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
        return this;
    }

    public SampleClaim withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public SampleClaim withMoreTimeRequested(boolean moreTimeRequested) {
        isMoreTimeRequested = moreTimeRequested;
        return this;
    }

    public SampleClaim withResponseDeadline(LocalDate responseDeadline) {
        this.responseDeadline = responseDeadline;
        return this;
    }

    public SampleClaim withSubmitterEmail(String submitterEmail) {
        this.submitterEmail = submitterEmail;
        return this;
    }

    public SampleClaim withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public SampleClaim withIssuedOn(LocalDate issuedOn) {
        this.issuedOn = issuedOn;
        return this;
    }

    public SampleClaim withCountyCourtJudgment(CountyCourtJudgment countyCourtJudgment) {
        this.countyCourtJudgment = countyCourtJudgment;
        return this;
    }

    public SampleClaim withCountyCourtJudgmentRequestedAt(LocalDateTime countyCourtJudgmentRequestedAt) {
        this.countyCourtJudgmentRequestedAt = countyCourtJudgmentRequestedAt;
        return this;
    }

    public SampleClaim withClaimData(ClaimData claimData) {
        this.claimData = claimData;
        return this;
    }

    public SampleClaim withRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
        return this;
    }

    public SampleClaim withResponse(Response response) {
        this.response = response;
        return this;
    }

    public SampleClaim withDefendantEmail(String defendantEmail) {
        this.defendantEmail = defendantEmail;
        return this;
    }

    public SampleClaim withSettlement(Settlement settlement) {
        this.settlement = settlement;
        return this;
    }

    public SampleClaim withSettlementReachedAt(LocalDateTime settlementReachedAt) {
        this.settlementReachedAt = settlementReachedAt;
        return this;
    }

    public SampleClaim withReDetermination(ReDetermination reDetermination) {
        this.reDetermination = reDetermination;
        return this;
    }

    public SampleClaim withReDeterminationRequestedAt(LocalDateTime reDeterminationRequestedAt) {
        this.reDeterminationRequestedAt = reDeterminationRequestedAt;
        return this;
    }

    public SampleClaim withState(ClaimState claimState) {
        this.state = claimState;
        return this;
    }

    public SampleClaim withSealedClaimDocument(URI sealedClaimDocument) {
        ClaimDocument claimDocument = ClaimDocument.builder()
            .documentManagementUrl(sealedClaimDocument)
            .documentName("001CLAIM-FORM")
            .documentType(SEALED_CLAIM)
            .createdDatetime(LocalDateTimeFactory.nowInLocalZone())
            .createdBy(OCMC)
            .build();
        this.claimDocumentCollection.addClaimDocument(claimDocument);
        return this;
    }

    public SampleClaim withClaimIssueReceiptDocument(URI uri) {
        ClaimDocument claimDocument = ClaimDocument.builder()
            .documentManagementUrl(uri)
            .documentName("claim-form-claimant-copy.pdf")
            .documentType(CLAIM_ISSUE_RECEIPT)
            .createdDatetime(LocalDateTimeFactory.nowInLocalZone())
            .createdBy(OCMC)
            .build();
        this.claimDocumentCollection.addClaimDocument(claimDocument);
        return this;
    }

    public SampleClaim withDefendantResponseReceiptDocument(URI uri) {
        ClaimDocument claimDocument = ClaimDocument.builder()
            .documentManagementUrl(uri)
            .documentName("claim-response.pdf")
            .documentType(DEFENDANT_RESPONSE_RECEIPT)
            .createdDatetime(LocalDateTimeFactory.nowInLocalZone())
            .createdBy(OCMC)
            .build();
        this.claimDocumentCollection.addClaimDocument(claimDocument);
        return this;
    }

    public SampleClaim withCCJRequestDocument(URI uri) {
        ClaimDocument claimDocument = ClaimDocument.builder()
            .documentManagementUrl(uri)
            .documentName("county-court-judgment-details.pdf")
            .documentType(CCJ_REQUEST)
            .createdDatetime(LocalDateTimeFactory.nowInLocalZone())
            .createdBy(OCMC)
            .build();
        this.claimDocumentCollection.addClaimDocument(claimDocument);
        return this;
    }

    public SampleClaim withSettlementAgreementDocument(URI uri) {
        ClaimDocument claimDocument = ClaimDocument.builder()
            .documentManagementUrl(uri)
            .documentName("settlement-agreement.pdf")
            .documentType(SETTLEMENT_AGREEMENT)
            .createdDatetime(LocalDateTimeFactory.nowInLocalZone())
            .createdBy(OCMC)
            .build();
        this.claimDocumentCollection.addClaimDocument(claimDocument);
        return this;
    }

    public SampleClaim withClaimantResponse(ClaimantResponse claimantResponse) {
        this.claimantResponse = claimantResponse;
        return this;
    }

    public SampleClaim withClaimantResponseDeadline(LocalDate claimantResponseDeadline) {
        this.claimantResponseDeadline = claimantResponseDeadline;
        return this;
    }

    public SampleClaim withClaimantRespondedAt(LocalDateTime localDateTime) {
        this.claimantRespondedAt = localDateTime;
        return this;
    }

    public SampleClaim withDirectionsQuestionnaireDeadline(LocalDate dqDeadline) {
        this.directionsQuestionnaireDeadline = dqDeadline;
        return this;
    }

    public SampleClaim withMoneyReceivedOn(LocalDate moneyReceivedOn) {
        this.moneyReceivedOn = moneyReceivedOn;
        return this;
    }

    public SampleClaim withFeatures(List<String> features) {
        this.features = features;
        return this;
    }

    public SampleClaim withReviewOrder(ReviewOrder reviewOrder) {
        this.reviewOrder = reviewOrder;
        return this;
    }

    public SampleClaim withDirectionOrder(DirectionOrder directionOrder) {
        this.directionOrder = directionOrder;
        return this;
    }

    public SampleClaim withClaimSubmissionOperationIndicators(
        ClaimSubmissionOperationIndicators claimSubmissionOperationIndicators
    ) {
        this.claimSubmissionOperationIndicators = claimSubmissionOperationIndicators;
        return this;
    }
}

