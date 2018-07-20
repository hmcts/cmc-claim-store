package uk.gov.hmcts.cmc.domain.constraints;

import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeNullError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.mayNotBeProvidedError;
import static uk.gov.hmcts.cmc.domain.constraints.utils.ConstraintsUtils.setValidationErrors;

public class ValidPartAdmissionConstraintValidator
    implements ConstraintValidator<ValidAdmission, PartAdmissionResponse> {

    static class Fields {
        static String PAYMENT_DECLARATION = "paymentDeclaration";
        static String PAYMENT_INTENTION = "paymentIntention";
        static String PAYMENT_OPTION = "paymentOption";
        static String STATEMENT_OF_MEANS = "statementOfMeans";

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

        if (response.getPaymentDeclaration().isPresent() && response.getPaymentIntention().isPresent()) {
            setValidationErrors(context, Fields.PAYMENT_DECLARATION, mayNotBeProvidedError(Fields.PAYMENT_INTENTION));
            setValidationErrors(context, Fields.PAYMENT_INTENTION, mayNotBeProvidedError(Fields.PAYMENT_DECLARATION));
            valid = false;
        } else if (!response.getPaymentDeclaration().isPresent() && !response.getPaymentIntention().isPresent()) {
            setValidationErrors(context, Fields.PAYMENT_DECLARATION, mayNotBeNullError(Fields.PAYMENT_INTENTION));
            setValidationErrors(context, Fields.PAYMENT_INTENTION, mayNotBeNullError(Fields.PAYMENT_DECLARATION));
            valid = false;
        }

        if (isDefendantIndividual(response.getDefendant())
            && !isPaymentOptionImmediately(response)
            && !isStatementOfMeansPopulated(response)) {
            setValidationErrorForMandatoryStatementOfMeans(context);
            valid = false;
        }

        if (isStatementOfMeansPopulated(response)
            && (isPaymentOptionImmediately(response) || !isDefendantIndividual(response.getDefendant()))) {
            setValidationErrorForPopulatedStatementOfMeans(context);
            valid = false;
        }

        return valid;
    }

    private static boolean isStatementOfMeansPopulated(PartAdmissionResponse response) {
        return response.getStatementOfMeans().isPresent();
    }

    private static boolean isDefendantIndividual(Party defendant) {
        return defendant instanceof Individual || defendant instanceof SoleTrader;

    }

    private static boolean isPaymentOptionImmediately(PartAdmissionResponse response) {
        return response.getPaymentIntention().isPresent()
            && !response.getPaymentIntention().get().getPaymentOption().equals(PaymentOption.IMMEDIATELY);
    }

    private static void setValidationErrorForMandatoryStatementOfMeans(ConstraintValidatorContext validatorContext) {
        setValidationErrors(
            validatorContext,
            Fields.STATEMENT_OF_MEANS,
            String.format(
                "may not be populated when %s is %s or defendant is business",
                Fields.PAYMENT_OPTION,
                PaymentOption.IMMEDIATELY.name()
            )
        );
    }

    private static void setValidationErrorForPopulatedStatementOfMeans(ConstraintValidatorContext validatorContext) {
        setValidationErrors(
            validatorContext,
            Fields.STATEMENT_OF_MEANS,
            mayNotBeProvidedError(
                Fields.PAYMENT_OPTION,
                "not" + PaymentOption.IMMEDIATELY.name()
            )
        );
    }
}
