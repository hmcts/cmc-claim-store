package uk.gov.hmcts.cmc.domain.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ValidUnemploymentConstraintValidator.class})
@Documented
public @interface ValidUnemployment {
    String message() default "Invalid unemployment";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
