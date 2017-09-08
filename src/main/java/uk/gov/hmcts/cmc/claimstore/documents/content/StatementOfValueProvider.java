package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.StatementOfValueContent;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.amount.Amount;
import uk.gov.hmcts.cmc.claimstore.models.amount.AmountRange;
import uk.gov.hmcts.cmc.claimstore.models.amount.NotKnown;
import uk.gov.hmcts.cmc.claimstore.models.particulars.PersonalInjury;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

@Component
public class StatementOfValueProvider {

    private static final String CAN_NOT_STATE = "The claimant can’t state the value of the claim. ";
    private static final String RECOVER_UP_TO = "The claimant expects to recover up to %s. ";
    private static final String WORTH_MORE_THAN = "The claimant estimates the claim to be worth more than %s. ";
    private static final String PERSONAL_INJURY = "This claim is for personal injury. ";
    private static final BigInteger AMOUNT = BigInteger.valueOf(1000);
    private static final String PERSONAL_INJURY_DAMAGES = "The claimant expects to recover %s "
        + formatMoney(AMOUNT) + " as general damages for pain, suffering and loss of amenity. ";
    private static final String HOUSING_DISREPAIR = "This claim is for housing disrepair and includes an order for "
        + "the landlord to carry out work. ";
    private static final String COST_OF_REPAIRS = "The cost of repairs or other work is %s "
        + formatMoney(AMOUNT) + ". ";
    private static final String OTHER_DAMAGES = "The cost of any claim for damages is %s "
        + formatMoney(AMOUNT) + ". ";
    private static final String ALSO_HOUSING_DISREPAIR = "This is also a claim for housing disrepair which includes "
        + "an order for the landlord to carry out work. ";


    public StatementOfValueContent create(final Claim claim) {

        StringBuilder personalInjuryContent = new StringBuilder();
        StringBuilder housingDisprepairContent = new StringBuilder();
        StringBuilder claimValueContent = new StringBuilder();

        final Optional<PersonalInjury> personalInjuryOptional = claim.getClaimData().getPersonalInjury();
        personalInjuryOptional.ifPresent(personalInjury -> {
            personalInjuryContent.append(PERSONAL_INJURY);
            personalInjuryContent.append(
                String.format(PERSONAL_INJURY_DAMAGES,
                    personalInjury.getGeneralDamages().getDisplayValue()));
        });

        claim.getClaimData().getHousingDisrepair().ifPresent(housingDisrepair -> {
            if (personalInjuryOptional.isPresent()) {
                housingDisprepairContent.append(ALSO_HOUSING_DISREPAIR);
            } else {
                housingDisprepairContent.append(HOUSING_DISREPAIR);
            }

            housingDisprepairContent.append(
                String.format(COST_OF_REPAIRS,
                    housingDisrepair.getCostOfRepairsDamages().getDisplayValue()));
            housingDisrepair.getOtherDamages().ifPresent(otherDamages ->
                housingDisprepairContent.append(String.format(OTHER_DAMAGES, otherDamages.getDisplayValue()))
            );
        });

        Amount claimValue = claim.getClaimData().getAmount();
        if (claimValue instanceof NotKnown) {
            claimValueContent.append(CAN_NOT_STATE);
        } else if (claimValue instanceof AmountRange) {
            final AmountRange amountRange = (AmountRange) claimValue;
            final Optional<BigDecimal> lowerValueOptional = amountRange.getLowerValue();
            if (lowerValueOptional.isPresent()) {
                final BigDecimal lowerValue = lowerValueOptional.get();
                claimValueContent.append(
                    String.format(RECOVER_UP_TO + WORTH_MORE_THAN,
                        formatMoney(amountRange.getHigherValue()),
                        formatMoney(lowerValue)
                    )
                );
            } else {
                claimValueContent.append(
                    String.format(RECOVER_UP_TO, formatMoney(amountRange.getHigherValue()))
                );
            }

        } else {
            throw new IllegalArgumentException("Amount is not valid type.");
        }

        return new StatementOfValueContent(personalInjuryContent.toString(),
            housingDisprepairContent.toString(),
            claimValueContent.toString());
    }

}
