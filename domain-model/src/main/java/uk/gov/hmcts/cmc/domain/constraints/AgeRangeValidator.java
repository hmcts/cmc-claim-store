package uk.gov.hmcts.cmc.domain.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = AgeRangeConstraintValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AgeRangeValidator {
    String message() default "Age must be between {minYears} and {maxYears}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int minYears() default 18;

    int maxYears() default 150;
}
