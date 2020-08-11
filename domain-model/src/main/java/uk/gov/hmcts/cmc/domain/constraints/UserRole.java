package uk.gov.hmcts.cmc.domain.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {UserRoleConstraintValidator.class})
@Documented
public @interface UserRole {
    String message() default "Invalid Role";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    List<String> userRoles = Arrays.asList("cmc-new-features-consent-given",
        "cmc-new-features-consent-not-given");
}
