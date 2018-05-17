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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest.noInterestBuilder;

public class SampleClaimData {

    private UUID externalId = UUID.randomUUID();
    private List<Party> claimants = singletonList(SampleParty.builder().individual());
    private List<TheirDetails> defendants = singletonList(SampleTheirDetails.builder().individualDetails());
    private Payment payment = SamplePayment.validDefaults();
    private Amount amount = SampleAmountBreakdown.validDefaults();
    private Interest interest = SampleInterest.standard();
    private String reason = "reason";
    private BigInteger feeAmount = new BigInteger("4000");
    private String feeAccountNumber = "PBA1234567";
    private StatementOfTruth statementOfTruth = new StatementOfTruth(claimants.get(0).getName(), "Director");
    private PersonalInjury personalInjury = new PersonalInjury(DamagesExpectation.MORE_THAN_THOUSAND_POUNDS);
    private String externalReferenceNumber = "CLAIM234324";
    private String preferredCourt = "LONDON COUNTY COUNCIL";
    private String feeCode = "X0012";
    private Timeline timeline = SampleTimeline.validDefaults();
    private Evidence evidence = SampleEvidence.validDefaults();

    private HousingDisrepair housingDisrepair = new HousingDisrepair(
        DamagesExpectation.MORE_THAN_THOUSAND_POUNDS,
        DamagesExpectation.MORE_THAN_THOUSAND_POUNDS
    );

    public static SampleClaimData builder() {
        return new SampleClaimData();
    }

    public SampleClaimData withExternalId(UUID externalId) {
        this.externalId = externalId;
        return this;
    }

    public SampleClaimData withHousingDisrepair(HousingDisrepair housingDisrepair) {
        this.housingDisrepair = housingDisrepair;
        return this;
    }

    public SampleClaimData withPersonalInjury(PersonalInjury personalInjury) {
        this.personalInjury = personalInjury;
        return this;
    }

    public SampleClaimData addClaimant(Party claimant) {
        this.claimants.add(claimant);
        return this;
    }

    public SampleClaimData addClaimants(List<Party> claimants) {
        this.claimants.addAll(claimants);
        return this;
    }

    public SampleClaimData clearClaimants() {
        this.claimants = new ArrayList<>();
        return this;
    }

    public SampleClaimData clearDefendants() {
        this.defendants = new ArrayList<>();
        return this;
    }

    public SampleClaimData withClaimants(List<Party> claimants) {
        this.claimants = claimants;
        return this;
    }

    public SampleClaimData withClaimant(Party party) {
        this.claimants = singletonList(party);
        return this;
    }

    public SampleClaimData addDefendant(TheirDetails defendant) {
        this.defendants.add(defendant);
        return this;
    }

    public SampleClaimData addDefendants(List<TheirDetails> defendants) {
        this.defendants.addAll(defendants);
        return this;
    }

    public SampleClaimData withDefendants(List<TheirDetails> defendants) {
        this.defendants = defendants;
        return this;
    }

    public SampleClaimData withPayment(Payment payment) {
        this.payment = payment;
        return this;
    }

    public SampleClaimData withDefendant(TheirDetails defendant) {
        this.defendants = singletonList(defendant);
        return this;
    }

    public SampleClaimData withFeeAmount(BigInteger feeAmount) {
        this.feeAmount = feeAmount;
        return this;
    }

    public SampleClaimData withInterest(Interest interest) {
        this.interest = interest;
        return this;
    }

    public SampleClaimData withStatementOfTruth(StatementOfTruth statementOfTruth) {
        this.statementOfTruth = statementOfTruth;
        return this;
    }

    public SampleClaimData withFeeAccountNumber(String feeAccountNumber) {
        this.feeAccountNumber = feeAccountNumber;
        return this;
    }

    public SampleClaimData withExternalReferenceNumber(String externalReferenceNumber) {
        this.externalReferenceNumber = externalReferenceNumber;
        return this;
    }

    public SampleClaimData withPreferredCourt(String preferredCourt) {
        this.preferredCourt = preferredCourt;
        return this;
    }

    public SampleClaimData withFeeCode(String feeCode) {
        this.feeCode = feeCode;
        return this;
    }

    public SampleClaimData withAmount(Amount amount) {
        this.amount = amount;
        return this;
    }

    public SampleClaimData withTimeline(Timeline timeline) {
        this.timeline = timeline;
        return this;
    }

    public SampleClaimData withEvidence(Evidence evidence) {
        this.evidence = evidence;
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
            evidence);
    }

    public static ClaimData validDefaults() {
        return builder().build();
    }

    public static ClaimData submittedByClaimant() {
        return submittedByClaimantBuilder().build();
    }

    public static SampleClaimData submittedByClaimantBuilder() {
        return builder()
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
                .individualDetails())
            .withTimeline(SampleTimeline.validDefaults())
            .withEvidence(SampleEvidence.validDefaults());
    }

    public static ClaimData submittedByLegalRepresentative() {
        return submittedByLegalRepresentativeBuilder().build();
    }

    public static SampleClaimData submittedByLegalRepresentativeBuilder() {
        return new SampleClaimData()
            .withAmount(SampleAmountRange.validDefaults());
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
            .withAmount(SampleAmountBreakdown.validDefaults())
            .build();
    }
}
