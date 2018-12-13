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
                    if (!Objects.equals(interest.getRate(), ccdCase.getInterestRate())) {
                        failWithMessage("Expected CCDCase.interestRate to be <%s> but was <%s>",
                            ccdCase.getInterestRate(), interest.getRate());
                    }
                    if (!Objects.equals(interest.getType(), ccdCase.getInterestType())) {
                        failWithMessage("Expected CCDCase.interestType to be <%s> but was <%s>",
                            ccdCase.getInterestType(), interest.getType());
                    }
                    if (!Objects.equals(interest.getReason(), ccdCase.getInterestReason())) {
                        failWithMessage("Expected CCDCase.interestReason to be <%s> but was <%s>",
                            ccdCase.getInterestReason(), interest.getRate());
                    }
                    interest.getSpecificDailyAmount().ifPresent(dailyAmount -> {
                        if (!Objects.equals(dailyAmount, ccdCase.getInterestSpecificDailyAmount())) {
                            failWithMessage("Expected CCDCase.interestSpecificDailyAmount to be <%s> but was <%s>",
                                ccdCase.getInterestSpecificDailyAmount(), dailyAmount);

                        }
                    });
                    Optional.ofNullable(interest.getInterestDate())
                        .ifPresent(interestDate -> {
                            if (!Objects.equals(interestDate.getDate(), ccdCase.getInterestClaimStartDate())) {
                                failWithMessage("Expected CCDCase.interestClaimStartDate to be <%s> but was <%s>",
                                    ccdCase.getInterestClaimStartDate(), interestDate.getDate());
                            }
                            if (!Objects.equals(interestDate.getType(), ccdCase.getInterestDateType())) {
                                failWithMessage("Expected CCDCase.interestDateType to be <%s> but was <%s>",
                                    ccdCase.getInterestDateType(), interestDate.getType());
                            }

                            if (!Objects.equals(interestDate.getReason(), ccdCase.getInterestStartDateReason())) {
                                failWithMessage("Expected CCDCase.interestStartDateReason to be <%s> but was <%s>",
                                    ccdCase.getInterestStartDateReason(), interestDate.getReason());
                            }

                            if (!Objects.equals(interestDate.getEndDateType(), ccdCase.getInterestEndDateType())) {
                                failWithMessage("Expected CCDCase.interestEndDateType to be <%s> but was <%s>",
                                    ccdCase.getInterestEndDateType(), interestDate.getEndDateType());
                            }
                        });
                }
            );

        Optional.ofNullable(actual.getClaimData().getPayment())
            .ifPresent(payment -> {
                    if (!Objects.equals(payment.getId(), ccdCase.getPaymentId())) {
                        failWithMessage("Expected CCDCase.paymentId to be <%s> but was <%s>",
                            ccdCase.getPaymentId(), payment.getId());
                    }
                    if (!Objects.equals(payment.getReference(), ccdCase.getPaymentReference())) {
                        failWithMessage("Expected CCDCase.paymentReference to be <%s> but was <%s>",
                            ccdCase.getPaymentReference(), payment.getReference());
                    }
                    if (!Objects.equals(payment.getAmount(), ccdCase.getPaymentAmount())) {
                        failWithMessage("Expected CCDCase.paymentAmount to be <%s> but was <%s>",
                            ccdCase.getPaymentAmount(), payment.getAmount());
                    }
                    if (!Objects.equals(payment.getDateCreated(), ccdCase.getPaymentDateCreated())) {
                        failWithMessage("Expected CCDCase.paymentDateCreated to be <%s> but was <%s>",
                            ccdCase.getPaymentDateCreated(), payment.getDateCreated());
                    }
                    if (!Objects.equals(payment.getStatus(), ccdCase.getPaymentStatus())) {
                        failWithMessage("Expected CCDCase.paymentStatus to be <%s> but was <%s>",
                            ccdCase.getPaymentStatus(), payment.getStatus());
                    }
                }
            );

        actual.getClaimData().getPersonalInjury()
            .ifPresent(personalInjury ->
            {
                if (!Objects.equals(personalInjury.getGeneralDamages().name(),
                    ccdCase.getPersonalInjuryGeneralDamages())) {
                    failWithMessage("Expected CCDCase.personalInjuryGeneralDamages to be <%s> but was <%s>",
                        ccdCase.getPersonalInjuryGeneralDamages(), personalInjury.getGeneralDamages());
                }
            });


        actual.getClaimData().getHousingDisrepair().ifPresent(housingDisrepair ->
        {
            if (!Objects.equals(housingDisrepair.getCostOfRepairsDamages().name(), ccdCase.getHousingDisrepairCostOfRepairDamages())) {
                failWithMessage("Expected CCDCase.housingDisrepairCostOfRepairDamages to be <%s> but was <%s>",
                    ccdCase.getHousingDisrepairCostOfRepairDamages(), housingDisrepair.getCostOfRepairsDamages());
            }
            housingDisrepair.getOtherDamages().ifPresent(damagesExpectation ->
            {
                if (!Objects.equals(damagesExpectation.name(), ccdCase.getHousingDisrepairOtherDamages())) {
                    failWithMessage("Expected CCDCase.housingDisrepairOtherDamages to be <%s> but was <%s>",
                        ccdCase.getHousingDisrepairOtherDamages(), damagesExpectation.name());
                }
            });

        });

        actual.getClaimData().getStatementOfTruth().ifPresent(statementOfTruth ->
        {
            if (!Objects.equals(statementOfTruth.getSignerName(), ccdCase.getSotSignerName())) {
                failWithMessage("Expected CCDCase.amountLowerValue to be <%s> but was <%s>",
                    ccdCase.getSotSignerName(), statementOfTruth.getSignerName());
            }

            if (!Objects.equals(statementOfTruth.getSignerRole(), ccdCase.getSotSignerRole())) {
                failWithMessage("Expected CCDCase.sotSignerRole to be <%s> but was <%s>",
                    ccdCase.getSotSignerRole(), statementOfTruth.getSignerRole());
            }
        });

        org.assertj.core.api.Assertions.assertThat(actual.getClaimData().getClaimants().size()).isEqualTo(ccdCase.getClaimants().size());
        org.assertj.core.api.Assertions.assertThat(actual.getClaimData().getDefendants().size()).isEqualTo(ccdCase.getDefendants().size());


        return this;
    }

}
