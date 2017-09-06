package uk.gov.hmcts.cmc.claimstore.utils;

import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

public class BeanValidator {

    private BeanValidator() {
        // Utility class
    }

    private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    public static <T> Set<String> validate(T bean) {
        return getMessages(factory.getValidator().validate(bean));
    }

    private static <T> Set<String> getMessages(final Set<ConstraintViolation<T>> response) {
        return response.stream()
            .map(r -> r.getPropertyPath() + " : " + r.getMessage())
            .collect(Collectors.toSet());
    }

}
