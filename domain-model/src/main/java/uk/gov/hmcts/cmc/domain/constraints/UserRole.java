package uk.gov.hmcts.cmc.domain.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {UserRoleConstraintValidator.class})
@Documented
public @interface UserRole {
    String message() default "Invalid Role";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
