package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class ClaimDataAssert extends AbstractAssert<ClaimDataAssert, ClaimData> {

    public ClaimDataAssert(ClaimData claimData) {
        super(claimData, ClaimDataAssert.class);
    }

    public ClaimDataAssert isEqualTo(CCDClaim ccdClaim) {
        isNotNull();

        if (!Objects.equals(actual.getReason(), ccdClaim.getReason())) {
            failWithMessage("Expected CCDClaim.reason to be <%s> but was <%s>",
                ccdClaim.getReason(), actual.getReason());
        }

        if (!Objects.equals(actual.getFeeCode().orElse(null), ccdClaim.getFeeCode())) {
            failWithMessage("Expected CCDClaim.feeCode to be <%s> but was <%s>",
                ccdClaim.getFeeCode(), actual.getFeeCode().orElse(null));
        }

        if (!Objects.equals(actual.getFeeAccountNumber().orElse(null), ccdClaim.getFeeAccountNumber())) {
            failWithMessage("Expected CCDClaim.feeAccountNumber to be <%s> but was <%s>",
                ccdClaim.getFeeAccountNumber(), actual.getFeeAccountNumber().orElse(null));
        }

        if (!Objects.equals(actual.getFeeAmountInPennies(), ccdClaim.getFeeAmountInPennies())) {
            failWithMessage("Expected CCDClaim.feeAmountInPennies to be <%s> but was <%s>",
                ccdClaim.getFeeAmountInPennies(), actual.getFeeAmountInPennies());
        }

        if (!Objects.equals(actual.getExternalReferenceNumber().orElse(null),
            ccdClaim.getExternalReferenceNumber())) {
            failWithMessage("Expected CCDClaim.externalReferenceNumber to be <%s> but was <%s>",
                ccdClaim.getExternalReferenceNumber(), actual.getExternalReferenceNumber().orElse(null));
        }

        if (!Objects.equals(actual.getExternalId().toString(), ccdClaim.getExternalId())) {
            failWithMessage("Expected CCDClaim.externalId to be <%s> but was <%s>",
                ccdClaim.getExternalId(), actual.getExternalId());
        }

        if (!Objects.equals(actual.getPreferredCourt().orElse(null), ccdClaim.getPreferredCourt())) {
            failWithMessage("Expected CCDClaim.preferredCourt to be <%s> but was <%s>",
                ccdClaim.getPreferredCourt(), actual.getPreferredCourt().orElse(null));
        }

        assertThat(actual.getAmount()).isEqualTo(ccdClaim.getAmount());

        Optional.ofNullable(actual.getInterest())
            .ifPresent(interest -> assertThat(interest).isEqualTo(ccdClaim.getInterest())
            );

        Optional.ofNullable(actual.getPayment())
            .ifPresent(payment -> assertThat(payment).isEqualTo(ccdClaim.getPayment())
            );

        actual.getPersonalInjury()
            .ifPresent(personalInjury ->
                assertThat(personalInjury.getGeneralDamages().name())
                    .isEqualTo(ccdClaim.getPersonalInjury().getGeneralDamages()));


        actual.getHousingDisrepair().ifPresent(housingDisrepair ->
            housingDisrepair.getOtherDamages().ifPresent(otherDamages ->
                assertThat(otherDamages.name()).isEqualTo(ccdClaim.getHousingDisrepair().getOtherDamages())));

        actual.getStatementOfTruth().ifPresent(statementOfTruth ->
            assertThat(statementOfTruth).isEqualTo(ccdClaim.getStatementOfTruth()));

        assertThat(actual.getClaimants().size()).isEqualTo(ccdClaim.getClaimants().size());
        assertThat(actual.getDefendants().size()).isEqualTo(ccdClaim.getDefendants().size());

        return this;
    }
}
