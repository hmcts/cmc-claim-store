package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmountRow;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.AmountRow;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;
import uk.gov.hmcts.cmc.domain.models.amount.NotKnown;

import java.util.Objects;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;

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

        if (!Objects.equals(actual.getCreatedAt(), ccdCase.getSubmittedOn())) {
            failWithMessage("Expected CCDCase.submittedOn to be <%s> but was <%s>",
                ccdCase.getSubmittedOn(), actual.getCreatedAt());
        }

        if (!Objects.equals(actual.getExternalId(), ccdCase.getExternalId())) {
            failWithMessage("Expected CCDCase.externalId to be <%s> but was <%s>",
                ccdCase.getExternalId(), actual.getExternalId());
        }

        if (!Objects.equals(actual.getIssuedOn(), ccdCase.getIssuedOn())) {
            failWithMessage("Expected CCDCase.issuedOn to be <%s> but was <%s>",
                ccdCase.getIssuedOn(), actual.getIssuedOn());
        }

        if (!Objects.equals(actual.getSubmitterEmail(), ccdCase.getSubmitterEmail())) {
            failWithMessage("Expected CCDCase.submitterEmail to be <%s> but was <%s>",
                ccdCase.getSubmitterEmail(), actual.getSubmitterEmail());
        }

        actual.getTotalAmountTillToday().ifPresent(totalAmount ->{
            if (!Objects.equals(totalAmount, ccdCase.getTotalAmount())) {
                failWithMessage("Expected CCDCase.totalAmount to be <%s> but was <%s>",
                    ccdCase.getTotalAmount(), totalAmount);
            }
        });

        ClaimData claimData = actual.getClaimData();
        if (!Objects.equals(claimData.getReason(), ccdCase.getReason())) {
            failWithMessage("Expected CCDClaim.reason to be <%s> but was <%s>",
                ccdCase.getReason(), claimData.getReason());
        }

        if (!Objects.equals(claimData.getFeeCode().orElse(null), ccdCase.getFeeCode())) {
            failWithMessage("Expected CCDClaim.feeCode to be <%s> but was <%s>",
                ccdCase.getFeeCode(), claimData.getFeeCode().orElse(null));
        }

        if (!Objects.equals(claimData.getFeeAccountNumber().orElse(null), ccdCase.getFeeAccountNumber())) {
            failWithMessage("Expected CCDClaim.feeAccountNumber to be <%s> but was <%s>",
                ccdCase.getFeeAccountNumber(), claimData.getFeeAccountNumber().orElse(null));
        }

        if (!Objects.equals(claimData.getFeeAmountInPennies(), ccdCase.getFeeAmountInPennies())) {
            failWithMessage("Expected CCDClaim.feeAmountInPennies to be <%s> but was <%s>",
                ccdCase.getFeeAmountInPennies(), claimData.getFeeAmountInPennies());
        }

        if (!Objects.equals(claimData.getExternalReferenceNumber().orElse(null),
            ccdCase.getExternalReferenceNumber())) {
            failWithMessage("Expected CCDClaim.externalReferenceNumber to be <%s> but was <%s>",
                ccdCase.getExternalReferenceNumber(), claimData.getExternalReferenceNumber().orElse(null));
        }

        if (!Objects.equals(claimData.getExternalId().toString(), ccdCase.getExternalId())) {
            failWithMessage("Expected CCDClaim.externalId to be <%s> but was <%s>",
                ccdCase.getExternalId(), claimData.getExternalId().toString());
        }

        if (!Objects.equals(claimData.getPreferredCourt().orElse(null), ccdCase.getPreferredCourt())) {
            failWithMessage("Expected CCDClaim.preferredCourt to be <%s> but was <%s>",
                ccdCase.getPreferredCourt(), claimData.getPreferredCourt().orElse(null));
        }

        Amount amount = claimData.getAmount();
        if (amount instanceof AmountBreakDown) {
            AmountBreakDown amountBreakDown = (AmountBreakDown) amount;

            AmountRow amountRow = amountBreakDown.getRows().get(0);
            CCDAmountRow ccdAmountRow = ccdCase.getAmountBreakDown().get(0).getValue();

            if (!Objects.equals(amountRow.getReason(), ccdAmountRow.getReason())) {
                failWithMessage("Expected CCDCase.amountRowReason to be <%s> but was <%s>",
                    ccdAmountRow.getReason(), amountRow.getReason());
            }

            if (!Objects.equals(amountRow.getAmount(), ccdAmountRow.getAmount())) {
                failWithMessage("Expected CCDCase.amount to be <%s> but was <%s>",
                    ccdAmountRow.getAmount(), amountRow.getAmount());
            }

        } else if (amount instanceof AmountRange) {

            AmountRange amountRange = (AmountRange) amount;

            if (!Objects.equals(amountRange.getHigherValue(), ccdCase.getAmountHigherValue())) {
                failWithMessage("Expected CCDCase.amountHigherValue to be <%s> but was <%s>",
                    ccdCase.getAmountHigherValue(), amountRange.getHigherValue());
            }

            amountRange.getLowerValue().ifPresent(lowerAmount -> {
                if (!Objects.equals(lowerAmount, ccdCase.getAmountLowerValue())) {
                    failWithMessage("Expected CCDCase.amountLowerValue to be <%s> but was <%s>",
                        ccdCase.getAmountLowerValue(), lowerAmount);
                }
            });
        } else {
            assertThat(amount).isInstanceOf(NotKnown.class);
        }

        ofNullable(claimData.getInterest()).ifPresent(interest -> {
                if (!Objects.equals(interest.getRate(), ccdCase.getInterestRate())) {
                    failWithMessage("Expected CCDCase.interestRate to be <%s> but was <%s>",
                        ccdCase.getInterestRate(), interest.getRate());
                }
                if (!Objects.equals(interest.getType().name(), ccdCase.getInterestType().name())) {
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
                ofNullable(interest.getInterestDate()).ifPresent(interestDate -> {
                    if (!Objects.equals(interestDate.getDate(), ccdCase.getInterestClaimStartDate())) {
                        failWithMessage("Expected CCDCase.interestClaimStartDate to be <%s> but was <%s>",
                            ccdCase.getInterestClaimStartDate(), interestDate.getDate());
                    }
                    if (!Objects.equals(interestDate.getType().name(), ccdCase.getInterestDateType().name())) {
                        failWithMessage("Expected CCDCase.interestDateType to be <%s> but was <%s>",
                            ccdCase.getInterestDateType(), interestDate.getType());
                    }

                    if (!Objects.equals(interestDate.getReason(), ccdCase.getInterestStartDateReason())) {
                        failWithMessage("Expected CCDCase.interestStartDateReason to be <%s> but was <%s>",
                            ccdCase.getInterestStartDateReason(), interestDate.getReason());
                    }

                    if (!Objects.equals(interestDate.getEndDateType().name(),
                        ccdCase.getInterestEndDateType().name())
                    ) {
                        failWithMessage("Expected CCDCase.interestEndDateType to be <%s> but was <%s>",
                            ccdCase.getInterestEndDateType(), interestDate.getEndDateType());
                    }
                });
            }
        );

        ofNullable(claimData.getPayment()).ifPresent(payment -> {
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

        claimData.getPersonalInjury().ifPresent(personalInjury -> {
            if (!Objects.equals(personalInjury.getGeneralDamages().name(),
                ccdCase.getPersonalInjuryGeneralDamages())) {
                failWithMessage("Expected CCDCase.personalInjuryGeneralDamages to be <%s> but was <%s>",
                    ccdCase.getPersonalInjuryGeneralDamages(), personalInjury.getGeneralDamages());
            }
        });

        claimData.getHousingDisrepair().ifPresent(housingDisrepair -> {
            if (!Objects.equals(housingDisrepair.getCostOfRepairsDamages().name(),
                ccdCase.getHousingDisrepairCostOfRepairDamages())
            ) {
                failWithMessage("Expected CCDCase.housingDisrepairCostOfRepairDamages to be <%s> but was <%s>",
                    ccdCase.getHousingDisrepairCostOfRepairDamages(), housingDisrepair.getCostOfRepairsDamages());
            }
            housingDisrepair.getOtherDamages().ifPresent(damagesExpectation -> {
                if (!Objects.equals(damagesExpectation.name(), ccdCase.getHousingDisrepairOtherDamages())) {
                    failWithMessage("Expected CCDCase.housingDisrepairOtherDamages to be <%s> but was <%s>",
                        ccdCase.getHousingDisrepairOtherDamages(), damagesExpectation.name());
                }
            });

        });

        claimData.getStatementOfTruth().ifPresent(statementOfTruth -> {
            if (!Objects.equals(statementOfTruth.getSignerName(), ccdCase.getSotSignerName())) {
                failWithMessage("Expected CCDCase.amountLowerValue to be <%s> but was <%s>",
                    ccdCase.getSotSignerName(), statementOfTruth.getSignerName());
            }

            if (!Objects.equals(statementOfTruth.getSignerRole(), ccdCase.getSotSignerRole())) {
                failWithMessage("Expected CCDCase.sotSignerRole to be <%s> but was <%s>",
                    ccdCase.getSotSignerRole(), statementOfTruth.getSignerRole());
            }
        });

        assertThat(claimData.getClaimants().size()).isEqualTo(ccdCase.getClaimants().size());
        assertThat(claimData.getDefendants().size()).isEqualTo(ccdCase.getDefendants().size());

        return this;
    }

}
