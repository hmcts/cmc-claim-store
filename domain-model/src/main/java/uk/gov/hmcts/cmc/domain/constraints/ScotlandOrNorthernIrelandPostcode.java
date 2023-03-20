package uk.gov.hmcts.cmc.domain.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = ScotlandOrNorthernIrelandPostcodeConstraintValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ScotlandOrNorthernIrelandPostcode {

    String message() default "Postcode is not of England or Wales format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
