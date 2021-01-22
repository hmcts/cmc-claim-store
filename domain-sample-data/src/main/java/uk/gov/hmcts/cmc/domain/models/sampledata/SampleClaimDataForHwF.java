package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.Timeline;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.particulars.DamagesExpectation;
import uk.gov.hmcts.cmc.domain.models.particulars.HousingDisrepair;
import uk.gov.hmcts.cmc.domain.models.particulars.PersonalInjury;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest.noInterestBuilder;

public class SampleClaimDataForHwF {
    public static final String EXTERNAL_REFERENCE_NUMBER = "CLAIM234324";
    private static final String RETURN_URL = "http://returnUrl.test";

    private UUID externalId = UUID.fromString(SampleClaim.EXTERNAL_ID);
    private List<Party> claimants;
    private List<TheirDetails> defendants;
    private Payment payment = SamplePayment.builder().build();
    private Amount amount = SampleAmountBreakdown.builder().build();
    private Interest interest = SampleInterest.standard();
    private String reason = "reason";
    private BigInteger feeAmount = BigInteger.valueOf(4000);
    private String moreInfoDetails = "Info";
    private BigInteger feeRemitted = BigInteger.valueOf(3000);
    private BigInteger feeAmountAfterRemission = BigInteger.valueOf(3000);
    private String feeAccountNumber = "PBA1234567";
    private StatementOfTruth statementOfTruth;
    private PersonalInjury personalInjury = new PersonalInjury(DamagesExpectation.MORE_THAN_THOUSAND_POUNDS);
    private String externalReferenceNumber = EXTERNAL_REFERENCE_NUMBER;
    private String preferredCourt = "LONDON COUNTY COUNCIL";
    private String feeCode = "X0012";
    private Timeline timeline = SampleTimeline.validDefaults();
    private Evidence evidence = SampleEvidence.validDefaults();
    private String helpWithFeesNumber = "HWF012345";
    private String helpWithFeesType = "Claim Issue";
    private String hwfFeeDetailsSummary = "Summary";
    private String hwfMandatoryDetails = "Details";
    private List<String> hwfMoreInfoNeededDocuments = asList("BANK_STATEMENTS", "PRISONERS_INCOME");
    private LocalDate hwfDocumentsToBeSentBefore;
    private HousingDisrepair housingDisrepair = new HousingDisrepair(
        DamagesExpectation.MORE_THAN_THOUSAND_POUNDS,
        DamagesExpectation.MORE_THAN_THOUSAND_POUNDS
    );

    public SampleClaimDataForHwF(List<Party> claimants, List<TheirDetails> defendants) {
        this.claimants = claimants;
        this.defendants = defendants;
        this.statementOfTruth = new StatementOfTruth(claimants.get(0).getName(), "Director");
    }

    public static SampleClaimDataForHwF builder(List<Party> claimants, List<TheirDetails> defendants) {
        return new SampleClaimDataForHwF(claimants, defendants);
    }

    public static SampleClaimDataForHwF builder() {
        return new SampleClaimDataForHwF(
            singletonList(SampleParty.builder().individual()),
            singletonList(SampleTheirDetails.builder().withPhone("0776655443322").individualDetails()));
    }

    public SampleClaimDataForHwF withExternalId(UUID externalId) {
        this.externalId = externalId;
        return this;
    }

    public SampleClaimDataForHwF withHousingDisrepair(HousingDisrepair housingDisrepair) {
        this.housingDisrepair = housingDisrepair;
        return this;
    }

    public SampleClaimDataForHwF withPersonalInjury(PersonalInjury personalInjury) {
        this.personalInjury = personalInjury;
        return this;
    }

    public SampleClaimDataForHwF addClaimant(Party claimant) {
        this.claimants.add(claimant);
        return this;
    }

    public SampleClaimDataForHwF addClaimants(List<Party> claimants) {
        this.claimants.addAll(claimants);
        return this;
    }

    public SampleClaimDataForHwF clearClaimants() {
        this.claimants = new ArrayList<>();
        return this;
    }

    public SampleClaimDataForHwF clearDefendants() {
        this.defendants = new ArrayList<>();
        return this;
    }

    public SampleClaimDataForHwF withClaimants(List<Party> claimants) {
        this.claimants = claimants;
        return this;
    }

    public SampleClaimDataForHwF withClaimant(Party party) {
        this.claimants = singletonList(party);
        return this;
    }

    public SampleClaimDataForHwF addDefendant(TheirDetails defendant) {
        this.defendants.add(defendant);
        return this;
    }

    public SampleClaimDataForHwF addDefendants(List<TheirDetails> defendants) {
        this.defendants.addAll(defendants);
        return this;
    }

    public SampleClaimDataForHwF withDefendants(List<TheirDetails> defendants) {
        this.defendants = defendants;
        return this;
    }

    public SampleClaimDataForHwF withPayment(Payment payment) {
        this.payment = payment;
        return this;
    }

    public SampleClaimDataForHwF withDefendant(TheirDetails defendant) {
        this.defendants = singletonList(defendant);
        return this;
    }

    public SampleClaimDataForHwF withFeeAmount(BigInteger feeAmount) {
        this.feeAmount = feeAmount;
        return this;
    }

    public SampleClaimDataForHwF withFeeRemitted(BigInteger feeRemitted) {
        this.feeRemitted = feeRemitted;
        return this;
    }

    public SampleClaimDataForHwF withInterest(Interest interest) {
        this.interest = interest;
        return this;
    }

    public SampleClaimDataForHwF withReason(String reason) {
        this.reason = reason;
        return this;
    }

    public SampleClaimDataForHwF withStatementOfTruth(StatementOfTruth statementOfTruth) {
        this.statementOfTruth = statementOfTruth;
        return this;
    }

    public SampleClaimDataForHwF withFeeAccountNumber(String feeAccountNumber) {
        this.feeAccountNumber = feeAccountNumber;
        return this;
    }

    public SampleClaimDataForHwF withExternalReferenceNumber(String externalReferenceNumber) {
        this.externalReferenceNumber = externalReferenceNumber;
        return this;
    }

    public SampleClaimDataForHwF withPreferredCourt(String preferredCourt) {
        this.preferredCourt = preferredCourt;
        return this;
    }

    public SampleClaimDataForHwF withFeeCode(String feeCode) {
        this.feeCode = feeCode;
        return this;
    }

    public SampleClaimDataForHwF withAmount(Amount amount) {
        this.amount = amount;
        return this;
    }

    public SampleClaimDataForHwF withTimeline(Timeline timeline) {
        this.timeline = timeline;
        return this;
    }

    public SampleClaimDataForHwF withEvidence(Evidence evidence) {
        this.evidence = evidence;
        return this;
    }

    public SampleClaimDataForHwF withHelpWithFeesNumber(String helpWithFeesNumber) {
        this.helpWithFeesNumber = helpWithFeesNumber;
        return this;
    }

    public SampleClaimDataForHwF withHelpWithFeesType(String helpWithFeesType) {
        this.helpWithFeesType = helpWithFeesType;
        return this;
    }

    public SampleClaimDataForHwF withMoreInfoDetails(String moreInfoDetails) {
        this.moreInfoDetails = moreInfoDetails;
        return this;
    }

    public SampleClaimDataForHwF withHwfFeeDetailsSummary(String hwfFeeDetailsSummary) {
        this.hwfFeeDetailsSummary = hwfFeeDetailsSummary;
        return this;
    }

    public SampleClaimDataForHwF withHwfMandatoryDetails(String hwfMandatoryDetails) {
        this.hwfMandatoryDetails = hwfMandatoryDetails;
        return this;
    }

    public SampleClaimDataForHwF withHwfMoreInfoNeededDocuments(List<String> hwfMoreInfoNeededDocuments) {
        this.hwfMoreInfoNeededDocuments = hwfMoreInfoNeededDocuments;
        return this;
    }

    public SampleClaimDataForHwF withHwfDocumentsToBeSentBefore(LocalDate hwfDocumentsToBeSentBefore) {
        this.hwfDocumentsToBeSentBefore = hwfDocumentsToBeSentBefore;
        return this;
    }

    public ClaimData build() {
        return new ClaimData(
            externalId,
            claimants,
            defendants,
            payment,
            amount,
            feeAmount,
            feeRemitted,
            feeAmountAfterRemission,
            interest,
            personalInjury,
            housingDisrepair,
            reason,
            statementOfTruth,
            feeAccountNumber,
            externalReferenceNumber,
            preferredCourt,
            feeCode,
            timeline,
            evidence,
            helpWithFeesNumber,
            moreInfoDetails,
            helpWithFeesType,
            hwfFeeDetailsSummary,
            hwfMandatoryDetails,
            hwfMoreInfoNeededDocuments,
            hwfDocumentsToBeSentBefore);
    }

    public static ClaimData validDefaults() {
        return builder().build();
    }

    public static ClaimData submittedWithAmountMoreThanThousand() {
        return submittedByClaimantBuilder()
            .withAmount(SampleAmountBreakdown.withThousandAsAmount().build())
            .build();
    }

    public static ClaimData submittedByClaimant() {
        return submittedByClaimantBuilder().build();
    }

    public static SampleClaimDataForHwF submittedByClaimantBuilder() {
        return builder()
            .withExternalId(UUID.randomUUID())
            .withFeeAccountNumber(null)
            .withStatementOfTruth(null)
            .withPersonalInjury(null)
            .withHousingDisrepair(null)
            .clearClaimants()
            .addClaimant(SampleParty.builder()
                .withRepresentative(null)
                .individual())
            .withDefendant(SampleTheirDetails.builder()
                .withRepresentative(null)
                .withPhone("0776655443322")
                .individualDetails())
            .withTimeline(SampleTimeline.validDefaults())
            .withHwfMandatoryDetails(null)
            .withFeeRemitted(null)
            .withEvidence(SampleEvidence.validDefaults());
    }

    public static ClaimData submittedByLegalRepresentative() {
        return submittedByLegalRepresentativeBuilder().build();
    }

    public static SampleClaimDataForHwF submittedByLegalRepresentativeBuilder() {
        return builder()
            .clearClaimants()
            .withExternalId(UUID.randomUUID())
            .withClaimant(SampleParty.builder()
                .withRepresentative(SampleRepresentative.builder().build())
                .individual())
            .withAmount(SampleAmountRange.builder().build());
    }

    public static ClaimData noInterest() {
        return builder()
            .withInterest(
                noInterestBuilder()
                    .withInterestDate(SampleInterestDate.builder()
                        .withType(null)
                        .withDate(null)
                        .withReason(null)
                        .build())
                    .build())
            .withAmount(SampleAmountBreakdown.builder().build())
            .build();
    }
}
