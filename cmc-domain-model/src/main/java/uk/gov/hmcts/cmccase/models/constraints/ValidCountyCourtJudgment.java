package uk.gov.hmcts.cmccase.models.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ValidCountyCourtJudgmentValidator.class})
@Documented
public @interface ValidCountyCourtJudgment {
    String message() default "Invalid county court judgment request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
