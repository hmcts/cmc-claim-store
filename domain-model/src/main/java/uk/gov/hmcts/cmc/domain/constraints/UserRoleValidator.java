package uk.gov.hmcts.cmc.domain.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {UserRoleConstraintValidator.class})
@Documented
public @interface UserRoleValidator {
    String message() default "Invalid Role";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
