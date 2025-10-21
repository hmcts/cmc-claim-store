package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.StatementOfValueContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;
import uk.gov.hmcts.cmc.domain.models.amount.NotKnown;
import uk.gov.hmcts.cmc.domain.models.particulars.PersonalInjury;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

@Component
public class StatementOfValueProvider {

    public static final String CAN_NOT_STATE = "The claimant can’t state the value of the claim. ";
    public static final String RECOVER_UP_TO = "The claimant expects to recover up to %s. ";
    public static final String WORTH_MORE_THAN = "The claimant estimates the claim to be worth more than %s. ";
    public static final String PERSONAL_INJURY = "This claim is for personal injury. ";
    public static final BigInteger AMOUNT = BigInteger.valueOf(1000);
    public static final String PERSONAL_INJURY_DAMAGES = "The claimant expects to recover %s "
        + formatMoney(AMOUNT) + " as general damages for pain, suffering and loss of amenity. ";
    public static final String HOUSING_DISREPAIR = "This claim is for housing disrepair and includes an order for "
        + "the landlord to carry out work. ";
    public static final String COST_OF_REPAIRS = "The cost of repairs or other work is %s "
        + formatMoney(AMOUNT) + ". ";
    public static final String OTHER_DAMAGES = "The cost of any claim for damages is %s "
        + formatMoney(AMOUNT) + ". ";
    public static final String ALSO_HOUSING_DISREPAIR = "This is also a claim for housing disrepair which includes "
        + "an order for the landlord to carry out work. ";

    public StatementOfValueContent create(Claim claim) {

        StringBuilder personalInjuryContent = new StringBuilder();
        StringBuilder housingDisprepairContent = new StringBuilder();
        StringBuilder claimValueContent = new StringBuilder();

        Optional<PersonalInjury> personalInjuryOptional = claim.getClaimData().getPersonalInjury();
        personalInjuryOptional.ifPresent(personalInjury -> personalInjuryContent.append(PERSONAL_INJURY)
            .append(String.format(PERSONAL_INJURY_DAMAGES,
                personalInjury.getGeneralDamages().getDisplayValue()))
        );

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
            AmountRange amountRange = (AmountRange) claimValue;
            Optional<BigDecimal> lowerValueOptional = amountRange.getLowerValue();
            if (lowerValueOptional.isPresent()) {
                BigDecimal lowerValue = lowerValueOptional.get();
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
