package uk.gov.hmcts.cmc.domain.constraints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestDate;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.cmc.domain.models.Interest.InterestType.BREAKDOWN;
import static uk.gov.hmcts.cmc.domain.models.Interest.InterestType.NO_INTEREST;
import static uk.gov.hmcts.cmc.domain.models.InterestDate.InterestDateType.SUBMISSION;


public class InterDependentFieldsConstraintValidator implements ConstraintValidator<InterDependentFields, Object> {
    private Logger logger = LoggerFactory.getLogger(InterDependentFieldsConstraintValidator.class);

    private String field;
    private String dependentField;
    private Validator validator = getValidator();

    @Override
    public void initialize(InterDependentFields annotation) {
        field = annotation.field();
        dependentField = annotation.dependentField();
    }

    @Override
    public boolean isValid(Object validateThis, ConstraintValidatorContext ctx) {
        if (validateThis == null) {
            throw new IllegalArgumentException("validateThis is null");
        }
        Field fieldObj;
        Field dependentFieldObj;
        try {
            fieldObj = validateThis.getClass().getDeclaredField(field);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid field name", e);
        }
        try {
            dependentFieldObj = validateThis.getClass().getDeclaredField(dependentField);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid dependentField name", e);
        }
        if (fieldObj == null || dependentFieldObj == null) {
            throw new IllegalArgumentException("Invalid field names");
        }

        try {
            fieldObj.setAccessible(true);
            dependentFieldObj.setAccessible(true);

            if (isBreakdownInterest(validateThis)) {
                return true;
            }

            switch (field) {
                case "interestDate":
                    return validateInterestDate(validateThis, ctx, fieldObj, dependentFieldObj);

                case "rate":
                    return validateRate(validateThis, ctx, fieldObj, dependentFieldObj);

                case "reason":
                    return validateReason(validateThis, ctx, fieldObj, dependentFieldObj);

                default:
                    return validateInputDate(validateThis, ctx, fieldObj, dependentFieldObj);
            }

        } catch (Exception e) {
            logger.trace(e.getMessage(), e);
            throw new IllegalArgumentException("Can't validate object", e);
        }
    }

    private boolean isBreakdownInterest(Object input) {
        return input instanceof Interest && ((Interest) input).getType() == BREAKDOWN;
    }

    private boolean validateRate(Object validateThis,
                                 ConstraintValidatorContext ctx,
                                 Field fieldObj,
                                 Field dependentFieldObj) throws IllegalAccessException {
        BigDecimal rate = (BigDecimal) fieldObj.get(validateThis);
        Interest.InterestType type = (Interest.InterestType) dependentFieldObj.get(validateThis);
        if ((type == null || !interestTypeIsNoInterest(type)) && rate == null) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate("may not be null")
                .addPropertyNode(field)
                .addConstraintViolation();

            return false;

        }
        return true;
    }

    private boolean validateReason(Object validateThis,
                                   ConstraintValidatorContext ctx,
                                   Field fieldObj,
                                   Field dependentFieldObj) throws IllegalAccessException {
        String reason = (String) fieldObj.get(validateThis);
        InterestDate.InterestDateType type = getInterestDateType(validateThis, dependentFieldObj);
        if ((type == null || !interestDateTypeIsSubmission(type)) && isBlank(reason)) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate("may not be empty")
                .addPropertyNode(field)
                .addConstraintViolation();

            return false;

        }
        return true;
    }

    private InterestDate.InterestDateType getInterestDateType(Object validateThis, Field dependentFieldObj)
        throws IllegalAccessException {
        return dependentFieldObj.get(validateThis) != null
            ? (InterestDate.InterestDateType) dependentFieldObj.get(validateThis)
            : null;
    }

    private boolean validateInputDate(Object validateThis,
                                      ConstraintValidatorContext ctx,
                                      Field fieldObj,
                                      Field dependentFieldObj) throws IllegalAccessException {
        LocalDate inputDate = (LocalDate) fieldObj.get(validateThis);
        InterestDate.InterestDateType type = getInterestDateType(validateThis, dependentFieldObj);
        if (type == null || !interestDateTypeIsSubmission(type)) {

            Set<ConstraintViolation<Object>> violations
                = Optional.ofNullable(inputDate).isPresent() ? validator.validate(inputDate) : Collections.emptySet();

            if (!violations.isEmpty()) {
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate(getViolationMessages(violations, ctx))
                    .addPropertyNode(field)
                    .addConstraintViolation();

                return false;
            }
        }
        return true;
    }


    private boolean interestDateTypeIsSubmission(InterestDate.InterestDateType type) {
        return SUBMISSION.equals(type);
    }

    private boolean validateInterestDate(Object validateThis,
                                         ConstraintValidatorContext ctx,
                                         Field fieldObj,
                                         Field dependentFieldObj) throws IllegalAccessException {
        InterestDate interestDate = (InterestDate) fieldObj.get(validateThis);
        Interest interest = (Interest) dependentFieldObj.get(validateThis);
        if (interest != null && !interestTypeIsNoInterest(interest.getType())) {
            Set<ConstraintViolation<Object>> violations = validator.validate(interestDate);
            if (!violations.isEmpty()) {
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate(getViolationMessages(violations, ctx))
                    .addPropertyNode(field)
                    .addConstraintViolation();

                return false;
            }
        }
        return true;
    }

    private String getViolationMessages(Set<ConstraintViolation<Object>> violations,
                                        ConstraintValidatorContext ctx) {
        return violations.stream()
            .map(v -> v.getPropertyPath() + " : " + v.getMessage())
            .sorted(String::compareTo)
            .reduce((v1, v2) -> v1 + ", " + v2)
            .orElse(ctx.getDefaultConstraintMessageTemplate());
    }

    private Validator getValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }

    private boolean interestTypeIsNoInterest(Interest.InterestType type) {
        return type.equals(NO_INTEREST);
    }

}
