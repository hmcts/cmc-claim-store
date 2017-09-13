package uk.gov.hmcts.cmc.claimstore.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import cz.jirutka.validator.collection.constraints.EachNotNull;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.hmcts.cmc.claimstore.constraints.InterDependentFields;
import uk.gov.hmcts.cmc.claimstore.models.amount.Amount;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.claimstore.models.particulars.HousingDisrepair;
import uk.gov.hmcts.cmc.claimstore.models.particulars.PersonalInjury;
import uk.gov.hmcts.cmc.claimstore.models.party.Party;
import uk.gov.hmcts.cmc.claimstore.utils.MonetaryConversions;
import uk.gov.hmcts.cmc.claimstore.utils.Optionals;

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

import static uk.gov.hmcts.cmc.claimstore.utils.ToStringStyle.ourStyle;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@InterDependentFields(field = "interestDate", dependentField = "interest")
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

    // Validated by InterDependentFields
    private final InterestDate interestDate;

    @Valid
    private final PersonalInjury personalInjury;

    @Valid
    private final HousingDisrepair housingDisrepair;

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
        final UUID externalId,
        final List<Party> claimants,
        final List<TheirDetails> defendants,
        final Payment payment,
        final Amount amount,
        final BigInteger feeAmountInPennies,
        final Interest interest,
        final InterestDate interestDate,
        final PersonalInjury personalInjury,
        final HousingDisrepair housingDisrepair,
        final String reason,
        final StatementOfTruth statementOfTruth,
        final String feeAccountNumber,
        final String externalReferenceNumber,
        final String preferredCourt,
        final String feeCode) {

        this.externalId = externalId != null ? externalId : UUID.randomUUID();
        this.claimants = claimants;
        this.defendants = defendants;
        this.payment = payment;
        this.amount = amount;
        this.feeAmountInPennies = feeAmountInPennies;
        this.interest = interest;
        this.interestDate = interestDate;
        this.personalInjury = personalInjury;
        this.housingDisrepair = housingDisrepair;
        this.reason = reason;
        this.statementOfTruth = statementOfTruth;
        this.feeAccountNumber = feeAccountNumber;
        this.externalReferenceNumber = externalReferenceNumber;
        this.preferredCourt = preferredCourt;
        this.feeCode = feeCode;
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

    public InterestDate getInterestDate() {
        return interestDate;
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
        return claimants.stream().flatMap(p -> Optionals.toStream(p.getRepresentative())).findFirst().isPresent();
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
            && Objects.equals(interestDate, that.interestDate)
            && Objects.equals(personalInjury, that.personalInjury)
            && Objects.equals(housingDisrepair, that.housingDisrepair)
            && Objects.equals(reason, that.reason)
            && Objects.equals(statementOfTruth, that.statementOfTruth)
            && Objects.equals(feeAccountNumber, that.feeAccountNumber)
            && Objects.equals(externalId, that.externalId)
            && Objects.equals(externalReferenceNumber, that.externalReferenceNumber)
            && Objects.equals(preferredCourt, that.preferredCourt)
            && Objects.equals(feeCode, that.feeCode);
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
            interestDate,
            personalInjury,
            housingDisrepair,
            reason,
            statementOfTruth,
            feeAccountNumber,
            externalId,
            externalReferenceNumber,
            preferredCourt,
            feeCode);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
