package uk.gov.hmcts.cmc.domain.models.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = InterDependentFieldsConstraintValidator.class)
@Documented
public @interface InterDependentFields {
    String message() default "doesn't have valid Attributes";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String field();

    String dependentField();


    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RUNTIME)
    @interface List {
        InterDependentFields[] value();
    }
}
