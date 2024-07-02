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
@Constraint(validatedBy = {ValidPaymentIntentionConstraintValidator.class})
@Documented
public @interface ValidPaymentIntention {
    String message() default "Invalid payment intention";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
