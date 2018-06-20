package uk.gov.hmcts.cmc.domain.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ValidEmploymentConstraintValidator.class})
@Documented
public @interface ValidEmployment {
    String message() default "Invalid employment";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
