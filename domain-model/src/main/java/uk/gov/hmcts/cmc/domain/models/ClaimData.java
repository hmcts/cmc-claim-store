package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.hmcts.cmc.domain.constraints.EachNotNull;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.particulars.HousingDisrepair;
import uk.gov.hmcts.cmc.domain.models.particulars.PersonalInjury;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaimData {
    @Valid
    private final UUID externalId;

    @Valid
    @NotEmpty
    @Size(max = 20, message = "at most {max} claimants are supported")
    @EachNotNull
    private final List<Party> claimants;

    @Valid
    @NotEmpty
    @Size(max = 20, message = "at most {max} defendants are supported")
    @EachNotNull
    private final List<TheirDetails> defendants;

    @Valid
    private final Payment payment;

    @Valid
    @NotNull
    private final Amount amount;

    @NotNull
    @Min(0)
    private final BigInteger feeAmountInPennies;

    private final String feeCode;

    @Valid
    private final Interest interest;

    @Valid
    private final PersonalInjury personalInjury;

    @Valid
    private final HousingDisrepair housingDisrepair;

    @Valid
    private final Timeline timeline;

    @Valid
    private final Evidence evidence;

    @Valid
    @NotBlank
    @Size(max = 99000)
    private final String reason;

    @Valid
    private final StatementOfTruth statementOfTruth;

    @Pattern(regexp = "^PBA[0-9]{7}$")
    private final String feeAccountNumber;

    @Size(max = 25, message = "must be at most {max} characters")
    private final String externalReferenceNumber;

    @Size(max = 80, message = "must be at most {max} characters")
    private final String preferredCourt;

    @SuppressWarnings("squid:S00107") // Number of method parameters
    public ClaimData(
        UUID externalId,
        List<Party> claimants,
        List<TheirDetails> defendants,
        Payment payment,
        Amount amount,
        BigInteger feeAmountInPennies,
        Interest interest,
        PersonalInjury personalInjury,
        HousingDisrepair housingDisrepair,
        String reason,
        StatementOfTruth statementOfTruth,
        String feeAccountNumber,
        String externalReferenceNumber,
        String preferredCourt,
        String feeCode,
        Timeline timeline,
        Evidence evidence
    ) {
        this.externalId = externalId != null ? externalId : UUID.randomUUID();
        this.claimants = claimants;
        this.defendants = defendants;
        this.payment = payment;
        this.amount = amount;
        this.feeAmountInPennies = feeAmountInPennies;
        this.interest = interest;
        this.personalInjury = personalInjury;
        this.housingDisrepair = housingDisrepair;
        this.reason = reason;
        this.statementOfTruth = statementOfTruth;
        this.feeAccountNumber = feeAccountNumber;
        this.externalReferenceNumber = externalReferenceNumber;
        this.preferredCourt = preferredCourt;
        this.feeCode = feeCode;
        this.timeline = timeline;
        this.evidence = evidence;
    }

    public List<Party> getClaimants() {
        return claimants;
    }

    public Amount getAmount() {
        return amount;
    }

    public BigInteger getFeeAmountInPennies() {
        return feeAmountInPennies;
    }

    public Interest getInterest() {
        return interest;
    }

    @JsonIgnore
    public Party getClaimant() {
        if (claimants.size() == 1) {
            return claimants.get(0);
        } else {
            throw new IllegalStateException("This claim has multiple claimants");
        }
    }

    @JsonIgnore
    public TheirDetails getDefendant() {
        if (defendants.size() == 1) {
            return defendants.get(0);
        } else {
            throw new IllegalStateException("This claim has multiple defendants");
        }
    }

    @JsonIgnore
    public Boolean isClaimantRepresented() {
        return claimants.stream().anyMatch(claimant -> claimant.getRepresentative().isPresent());
    }

    @JsonIgnore
    public BigDecimal getFeesPaidInPound() {
        return MonetaryConversions.penniesToPounds(new BigDecimal(feeAmountInPennies));
    }

    public List<TheirDetails> getDefendants() {
        return defendants;
    }

    public Optional<PersonalInjury> getPersonalInjury() {
        return Optional.ofNullable(personalInjury);
    }

    public Optional<HousingDisrepair> getHousingDisrepair() {
        return Optional.ofNullable(housingDisrepair);
    }

    public String getReason() {
        return reason;
    }

    public UUID getExternalId() {
        return externalId;
    }

    public Payment getPayment() {
        return payment;
    }

    public Optional<StatementOfTruth> getStatementOfTruth() {
        return Optional.ofNullable(statementOfTruth);
    }

    public Optional<String> getFeeAccountNumber() {
        return Optional.ofNullable(feeAccountNumber);
    }

    public Optional<String> getExternalReferenceNumber() {
        return Optional.ofNullable(externalReferenceNumber);
    }

    public Optional<String> getPreferredCourt() {
        return Optional.ofNullable(preferredCourt);
    }

    public Optional<String> getFeeCode() {
        return Optional.ofNullable(feeCode);
    }

    public Optional<Timeline> getTimeline() {
        return Optional.ofNullable(timeline);
    }

    public Optional<Evidence> getEvidence() {
        return Optional.ofNullable(evidence);
    }

    @Override
    @SuppressWarnings("squid:S1067") // Its generated code for equals sonar
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ClaimData that = (ClaimData) other;
        return Objects.equals(claimants, that.claimants)
            && Objects.equals(defendants, that.defendants)
            && Objects.equals(payment, that.payment)
            && Objects.equals(amount, that.amount)
            && Objects.equals(feeAmountInPennies, that.feeAmountInPennies)
            && Objects.equals(interest, that.interest)
            && Objects.equals(personalInjury, that.personalInjury)
            && Objects.equals(housingDisrepair, that.housingDisrepair)
            && Objects.equals(reason, that.reason)
            && Objects.equals(statementOfTruth, that.statementOfTruth)
            && Objects.equals(feeAccountNumber, that.feeAccountNumber)
            && Objects.equals(externalId, that.externalId)
            && Objects.equals(externalReferenceNumber, that.externalReferenceNumber)
            && Objects.equals(preferredCourt, that.preferredCourt)
            && Objects.equals(feeCode, that.feeCode)
            && Objects.equals(timeline, that.timeline)
            && Objects.equals(evidence, that.evidence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            claimants,
            defendants,
            payment,
            amount,
            feeAmountInPennies,
            interest,
            personalInjury,
            housingDisrepair,
            reason,
            statementOfTruth,
            feeAccountNumber,
            externalId,
            externalReferenceNumber,
            preferredCourt,
            feeCode,
            timeline,
            evidence);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
