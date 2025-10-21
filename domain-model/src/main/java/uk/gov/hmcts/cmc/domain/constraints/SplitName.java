package uk.gov.hmcts.cmc.domain.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// todo ROC-5160 delete this class once frontend is merged
@Constraint(validatedBy = SplitNameConstraintValidator.class)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SplitName {

    String message() default "Either name or both first and last name must be provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
