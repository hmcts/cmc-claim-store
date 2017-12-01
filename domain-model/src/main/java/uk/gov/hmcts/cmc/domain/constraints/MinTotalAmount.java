package uk.gov.hmcts.cmc.domain.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Constraint(validatedBy = MinTotalAmountValidator.class)
public @interface MinTotalAmount {

    String value();

    boolean inclusive() default true;

    String message() default "Total value of at least {value} is required";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
