package uk.gov.hmcts.cmc.domain.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ValidPaymentIntentionConstraintValidator.class})
@Documented
public @interface ValidPaymentIntention {
    String message() default "Invalid payment intention";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
