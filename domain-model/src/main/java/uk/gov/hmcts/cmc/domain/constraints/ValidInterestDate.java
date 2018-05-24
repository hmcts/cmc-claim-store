package uk.gov.hmcts.cmc.domain.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ValidInterestDateConstraintValidator.class })
@Documented
public @interface ValidInterestDate {
    String message() default "Invalid interest date";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
