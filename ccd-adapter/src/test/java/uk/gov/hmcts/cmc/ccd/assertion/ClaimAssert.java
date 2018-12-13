package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;

import java.util.Objects;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;


public class ClaimAssert extends AbstractAssert<ClaimAssert, Claim> {

    public ClaimAssert(Claim actual) {
        super(actual, ClaimAssert.class);
    }

    public ClaimAssert isEqualTo(CCDCase ccdCase) {
        isNotNull();

        if (!Objects.equals(actual.getReferenceNumber(), ccdCase.getReferenceNumber())) {
            failWithMessage("Expected CCDCase.referenceNumber to be <%s> but was <%s>",
                ccdCase.getReferenceNumber(), actual.getReferenceNumber());
        }

        if (!Objects.equals(actual.getSubmitterId(), ccdCase.getSubmitterId())) {
            failWithMessage("Expected CCDCase.submitterId to be <%s> but was <%s>",
                ccdCase.getSubmitterId(), actual.getSubmitterId());
        }

        if (!Objects.equals(actual.getCreatedAt().format(ISO_DATE_TIME), ccdCase.getSubmittedOn())) {
            failWithMessage("Expected CCDCase.submittedOn to be <%s> but was <%s>",
                ccdCase.getSubmittedOn(), actual.getCreatedAt().format(ISO_DATE_TIME));
        }

        if (!Objects.equals(actual.getExternalId(), ccdCase.getExternalId())) {
            failWithMessage("Expected CCDCase.externalId to be <%s> but was <%s>",
                ccdCase.getExternalId(), actual.getExternalId());
        }

        if (!Objects.equals(actual.getIssuedOn().format(ISO_DATE), ccdCase.getIssuedOn())) {
            failWithMessage("Expected CCDCase.issuedOn to be <%s> but was <%s>",
                ccdCase.getIssuedOn(), actual.getIssuedOn().format(ISO_DATE));
        }

        if (!Objects.equals(actual.getSubmitterEmail(), ccdCase.getSubmitterEmail())) {
            failWithMessage("Expected CCDCase.submitterEmail to be <%s> but was <%s>",
                ccdCase.getSubmitterEmail(), actual.getSubmitterEmail());
        }

        if (!Objects.equals(actual.getResponseDeadline(), ccdCase.getResponseDeadline())) {
            failWithMessage("Expected CCDCase.responseDeadline to be <%s> but was <%s>",
                ccdCase.getResponseDeadline(), actual.getResponseDeadline());
        }

        if (!Objects.equals(actual.isMoreTimeRequested(), ccdCase.getMoreTimeRequested() == YES)) {
            failWithMessage("Expected CCDCase.moreTimeRequested to be <%s> but was <%s>",
                ccdCase.getMoreTimeRequested(), actual.isMoreTimeRequested());
        }

        if (!Objects.equals(actual.getClaimData().getReason(), ccdCase.getReason())) {
            failWithMessage("Expected CCDClaim.reason to be <%s> but was <%s>",
                ccdCase.getReason(), actual.getClaimData().getReason());
        }

        if (!Objects.equals(actual.getClaimData().getFeeCode().orElse(null), ccdCase.getFeeCode())) {
            failWithMessage("Expected CCDClaim.feeCode to be <%s> but was <%s>",
                ccdCase.getFeeCode(), actual.getClaimData().getFeeCode().orElse(null));
        }

        if (!Objects.equals(actual.getClaimData().getFeeAccountNumber().orElse(null), ccdCase.getFeeAccountNumber())) {
            failWithMessage("Expected CCDClaim.feeAccountNumber to be <%s> but was <%s>",
                ccdCase.getFeeAccountNumber(), actual.getClaimData().getFeeAccountNumber().orElse(null));
        }

        if (!Objects.equals(actual.getClaimData().getFeeAmountInPennies(), ccdCase.getFeeAmountInPennies())) {
            failWithMessage("Expected CCDClaim.feeAmountInPennies to be <%s> but was <%s>",
                ccdCase.getFeeAmountInPennies(), actual.getClaimData().getFeeAmountInPennies());
        }

        if (!Objects.equals(actual.getClaimData().getExternalReferenceNumber().orElse(null),
            ccdCase.getExternalReferenceNumber())) {
            failWithMessage("Expected CCDClaim.externalReferenceNumber to be <%s> but was <%s>",
                ccdCase.getExternalReferenceNumber(), actual.getClaimData().getExternalReferenceNumber().orElse(null));
        }

        if (!Objects.equals(actual.getClaimData().getExternalId().toString(), ccdCase.getExternalId())) {
            failWithMessage("Expected CCDClaim.externalId to be <%s> but was <%s>",
                ccdCase.getExternalId(), actual.getExternalId());
        }

        if (!Objects.equals(actual.getClaimData().getPreferredCourt().orElse(null), ccdCase.getPreferredCourt())) {
            failWithMessage("Expected CCDClaim.preferredCourt to be <%s> but was <%s>",
                ccdCase.getPreferredCourt(), actual.getClaimData().getPreferredCourt().orElse(null));
        }

        Amount amount = actual.getClaimData().getAmount();
        if (amount instanceof AmountBreakDown) {
            AmountBreakDown amountBreakDown = (AmountBreakDown) amount;
        } else if (amount instanceof AmountRange) {

            AmountRange amountRange = (AmountRange) amount;

            if (!Objects.equals(amountRange.getHigherValue(), ccdCase.getAmountHigherValue())) {
                failWithMessage("Expected CCDCase.amountHigherValue to be <%s> but was <%s>",
                    ccdCase.getAmountHigherValue(), amountRange.getHigherValue());
            }

            amountRange.getLowerValue().ifPresent(lowerAmount ->
            {
                if (!Objects.equals(lowerAmount, ccdCase.getAmountLowerValue())) {
                    failWithMessage("Expected CCDCase.amountLowerValue to be <%s> but was <%s>",
                        ccdCase.getAmountLowerValue(), lowerAmount);
                }
            });
        }


        Optional.ofNullable(actual.getClaimData().getInterest())
            .ifPresent(interest ->
                {
                    if (!Objects.equals(lowerAmount, ccdCase.getAmountLowerValue())) {
                        failWithMessage("Expected CCDCase.amountLowerValue to be <%s> but was <%s>",
                            ccdCase.getAmountLowerValue(), lowerAmount);
                    }
                }
            );

        Optional.ofNullable(actual.getClaimData().getPayment())
            .ifPresent(payment -> {
                    if (!Objects.equals(lowerAmount, ccdCase.getAmountLowerValue())) {
                        failWithMessage("Expected CCDCase.amountLowerValue to be <%s> but was <%s>",
                            ccdCase.getAmountLowerValue(), lowerAmount);
                    }
                }
            );

        actual.getClaimData().getPersonalInjury()
            .ifPresent(personalInjury ->
            {
                if (!Objects.equals(lowerAmount, ccdCase.getAmountLowerValue())) {
                    failWithMessage("Expected CCDCase.amountLowerValue to be <%s> but was <%s>",
                        ccdCase.getAmountLowerValue(), lowerAmount);
                }
            });


        actual.getClaimData().getHousingDisrepair().ifPresent(housingDisrepair ->
        {
            if (!Objects.equals(lowerAmount, ccdCase.getAmountLowerValue())) {
                failWithMessage("Expected CCDCase.amountLowerValue to be <%s> but was <%s>",
                    ccdCase.getAmountLowerValue(), lowerAmount);
            }
        });

        actual.getClaimData().getStatementOfTruth().ifPresent(statementOfTruth ->
        {
            if (!Objects.equals(lowerAmount, ccdCase.getAmountLowerValue())) {
                failWithMessage("Expected CCDCase.amountLowerValue to be <%s> but was <%s>",
                    ccdCase.getAmountLowerValue(), lowerAmount);
            }
        });

        org.assertj.core.api.Assertions.assertThat(actual.getClaimData().getClaimants().size()).isEqualTo(ccdCase.getClaimants().size());
        org.assertj.core.api.Assertions.assertThat(actual.getClaimData().getDefendants().size()).isEqualTo(ccdCase.getDefendants().size());


        return this;
    }

}
