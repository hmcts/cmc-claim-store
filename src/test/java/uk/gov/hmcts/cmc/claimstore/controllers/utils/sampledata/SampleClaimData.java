package uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata;

import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.models.Interest;
import uk.gov.hmcts.cmc.claimstore.models.InterestDate;
import uk.gov.hmcts.cmc.claimstore.models.Payment;
import uk.gov.hmcts.cmc.claimstore.models.amount.Amount;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.claimstore.models.particulars.DamagesExpectation;
import uk.gov.hmcts.cmc.claimstore.models.particulars.HousingDisrepair;
import uk.gov.hmcts.cmc.claimstore.models.particulars.PersonalInjury;
import uk.gov.hmcts.cmc.claimstore.models.party.Party;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleAmountBreakdown;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleAmountRange;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleInterest;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleInterestDate;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SamplePayment;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleTheirDetails;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;

public class SampleClaimData {

    private UUID externalId = UUID.fromString("9f49d8df-b734-4e86-aeb6-e22f0c2ca78d");
    private Party claimant = SampleParty.builder().individual();
    private List<TheirDetails> defendants = singletonList(SampleTheirDetails.builder().individualDetails());
    private Payment payment = SamplePayment.validDefaults();
    private Amount amount = SampleAmountBreakdown.validDefaults();
    private Interest interest = SampleInterest.standard();
    private InterestDate interestDate = SampleInterestDate.validDefaults();
    private String reason = "reason";
    private BigInteger feeAmount = new BigInteger("4000");
    private String feeAccountNumber = "PBA1234567";
    private StatementOfTruth statementOfTruth = new StatementOfTruth(claimant.getName(), "Director");
    private PersonalInjury personalInjury = new PersonalInjury(DamagesExpectation.MORE_THAN_THOUSAND_POUNDS);
    private String externalReferenceNumber = "CLAIM234324";
    private String preferredCourt = "LONDON COUNTY COUNCIL";
    private String feeCode = "X0012";

    private HousingDisrepair housingDisrepair = new HousingDisrepair(DamagesExpectation.MORE_THAN_THOUSAND_POUNDS,
        DamagesExpectation.MORE_THAN_THOUSAND_POUNDS);

    public static SampleClaimData builder() {
        return new SampleClaimData();
    }

    public SampleClaimData withHousingDisrepair(HousingDisrepair housingDisrepair) {
        this.housingDisrepair = housingDisrepair;
        return this;
    }

    public SampleClaimData withPersonalInjury(PersonalInjury personalInjury) {
        this.personalInjury = personalInjury;
        return this;
    }

    public SampleClaimData withClaimant(Party claimant) {
        this.claimant = claimant;
        return this;
    }

    public SampleClaimData clearDefendants() {
        this.defendants = new ArrayList<>();
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

    public SampleClaimData withInterestDate(InterestDate interestDate) {
        this.interestDate = interestDate;
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

    public ClaimData build() {
        return new ClaimData(
            externalId,
            claimant,
            defendants,
            payment,
            amount,
            feeAmount,
            interest,
            interestDate,
            personalInjury,
            housingDisrepair,
            reason,
            statementOfTruth,
            feeAccountNumber,
            externalReferenceNumber,
            preferredCourt,
            feeCode);
    }

    public static ClaimData validDefaults() {
        return builder().build();
    }

    public static ClaimData submittedByClaimant() {
        return builder()
            .withFeeAccountNumber(null)
            .withStatementOfTruth(null)
            .withPersonalInjury(null)
            .withHousingDisrepair(null)
            .withClaimant(SampleParty.builder()
                .withRepresentative(null)
                .individual())
            .withDefendant(SampleTheirDetails.builder()
                .withRepresentative(null)
                .individualDetails())
            .build();
    }

    public static ClaimData noInterest() {
        return builder()
            .withInterest(SampleInterest.noInterest())
            .withInterestDate(SampleInterestDate.builder()
                .withType(null)
                .withDate(null)
                .withReason(null)
                .build())
            .withAmount(SampleAmountRange.validDefaults())
            .build();
    }

}
