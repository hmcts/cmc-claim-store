package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.ValidPartAdmissionConstraintValidator.Fields.PAYMENT_DECLARATION;
import static uk.gov.hmcts.cmc.domain.constraints.ValidPartAdmissionConstraintValidator.Fields.PAYMENT_INTENTION;
import static uk.gov.hmcts.cmc.domain.constraints.ValidPartAdmissionConstraintValidator.Fields.PAYMENT_OPTION;
import static uk.gov.hmcts.cmc.domain.constraints.ValidPartAdmissionConstraintValidator.Fields.STATEMENT_OF_MEANS;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeNullError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeProvidedError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.IMMEDIATELY;

public class ValidPartAdmissionConstraintValidator
    implements ConstraintValidator<ValidAdmission, PartAdmissionResponse> {

    private static final String SOM_PROVIDED_ERROR = mayNotBeProvidedError(PAYMENT_OPTION, IMMEDIATELY.name());
    private static final String SOM_NOT_PROVIDED_ERROR = mayNotBeNullError(PAYMENT_OPTION, "not " + IMMEDIATELY.name());

    static class Fields {
        static final String PAYMENT_DECLARATION = "paymentDeclaration";
        static final String PAYMENT_INTENTION = "paymentIntention";
        static final String PAYMENT_OPTION = "paymentOption";
        static final String STATEMENT_OF_MEANS = "statementOfMeans";

        private Fields() {
            // NO-OP
        }
    }

    @Override
    public boolean isValid(PartAdmissionResponse response, ConstraintValidatorContext context) {
        if (response == null) {
            return true;
        }

        boolean valid = true;

        boolean isIndividual = isDefendantIndividual(response.getDefendant());
        boolean isImmediately = isPaymentOptionImmediately(response);
        boolean isSoMPopulated = response.getStatementOfMeans().isPresent();
        boolean isDeclarationPopulated = response.getPaymentDeclaration().isPresent();
        boolean isIntentionPopulated = response.getPaymentIntention().isPresent();

        if (isIntentionPopulated) {
            if (isDeclarationPopulated) {
                setValidationErrors(context, PAYMENT_DECLARATION, mayNotBeProvidedError(PAYMENT_INTENTION));
                setValidationErrors(context, PAYMENT_INTENTION, mayNotBeProvidedError(PAYMENT_DECLARATION));
                valid = false;
            }

            if (isIndividual && !isImmediately && !isSoMPopulated) {
                setValidationErrors(context, STATEMENT_OF_MEANS, SOM_NOT_PROVIDED_ERROR);
                valid = false;
            }

            if (isImmediately && isSoMPopulated) {
                setValidationErrors(context, STATEMENT_OF_MEANS, SOM_PROVIDED_ERROR);
                valid = false;
            }

        } else if (!isDeclarationPopulated) {
            setValidationErrors(context, PAYMENT_DECLARATION, mayNotBeNullError(PAYMENT_INTENTION));
            setValidationErrors(context, PAYMENT_INTENTION, mayNotBeNullError(PAYMENT_DECLARATION));
            valid = false;
        }

        return valid;
    }

    private static boolean isDefendantIndividual(Party defendant) {
        return defendant instanceof Individual || defendant instanceof SoleTrader;
    }

    private static boolean isPaymentOptionImmediately(PartAdmissionResponse response) {
        return response.getPaymentIntention()
            .map(item -> item.getPaymentOption().equals(IMMEDIATELY))
            .orElse(false);
    }
}
