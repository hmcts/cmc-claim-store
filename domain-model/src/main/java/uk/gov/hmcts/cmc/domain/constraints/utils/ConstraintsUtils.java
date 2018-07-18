package uk.gov.hmcts.cmc.domain.constraints.utils;

import javax.validation.ConstraintValidatorContext;

public class ConstraintsUtils {

    private ConstraintsUtils() {
        // NO-OP
    }

    public static void setValidationErrors(
        ConstraintValidatorContext validatorContext, String fieldName, String... errors) {

        validatorContext.disableDefaultConstraintViolation();
        for (String error : errors) {
            validatorContext.buildConstraintViolationWithTemplate(error)
                .addPropertyNode(fieldName)
                .addConstraintViolation();
        }
    }

    public static String mayNotBeNullError(String field) {
        return String.format("may not be null when %s is not provided", field);
    }

    public static String mayNotBeNullError(String field, String value) {
        return String.format("may not be null when %s is '%s'", field, value);
    }

    public static String mayNotBeProvidedError(String field) {
        return String.format("may not be provided when %s is provided", field);
    }

    public static String mayNotBeProvidedError(String field, String value) {
        return String.format("may not be provided when %s is '%s'", field, value);
    }
}
