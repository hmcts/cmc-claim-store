package uk.gov.hmcts.cmc.domain.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ValidInterestDateConstraintValidator.class })
@Documented
public @interface ValidInterestDate {
    String message() default "Invalid interest date";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
