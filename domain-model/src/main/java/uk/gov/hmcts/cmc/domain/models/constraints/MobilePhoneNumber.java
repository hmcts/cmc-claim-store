package uk.gov.hmcts.cmc.domain.models.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = MobilePhoneNumberConstraintValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MobilePhoneNumber {
    String message() default "Mobile number is not valid UK number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
