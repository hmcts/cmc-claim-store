package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.ValidFullAdmissionConstraintValidator.Fields.PAYMENT_OPTION;
import static uk.gov.hmcts.cmc.domain.constraints.ValidFullAdmissionConstraintValidator.Fields.STATEMENT_OF_MEANS;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeNullError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeProvidedError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.IMMEDIATELY;

public class ValidFullAdmissionConstraintValidator
    implements ConstraintValidator<ValidAdmission, FullAdmissionResponse> {

    private static final String SOM_PROVIDED_ERROR = mayNotBeProvidedError(PAYMENT_OPTION, IMMEDIATELY.name());
    private static final String SOM_NOT_PROVIDED_ERROR = mayNotBeNullError(PAYMENT_OPTION, "not " + IMMEDIATELY.name());

    static class Fields {
        static final String PAYMENT_OPTION = "paymentOption";
        static final String STATEMENT_OF_MEANS = "statementOfMeans";

        private Fields() {
            // NO-OP
        }
    }

    @Override
    public boolean isValid(FullAdmissionResponse response, ConstraintValidatorContext context) {
        if (response == null) {
            return true;
        }

        boolean valid = true;

        boolean isIndividual = isDefendantIndividual(response.getDefendant());
        boolean isImmediately = isPaymentOptionImmediately(response);
        boolean isSoMPopulated = response.getStatementOfMeans().isPresent();

        if (isIndividual && !isImmediately && !isSoMPopulated) {
            setValidationErrors(context, STATEMENT_OF_MEANS, SOM_NOT_PROVIDED_ERROR);
            valid = false;
        }

        if (isImmediately && isSoMPopulated) {
            setValidationErrors(context, STATEMENT_OF_MEANS, SOM_PROVIDED_ERROR);
            valid = false;
        }

        return valid;
    }

    private static boolean isDefendantIndividual(Party defendant) {
        return defendant instanceof Individual || defendant instanceof SoleTrader;
    }

    private static boolean isPaymentOptionImmediately(FullAdmissionResponse response) {
        return response.getPaymentIntention().getPaymentOption().equals(IMMEDIATELY);
    }
}
