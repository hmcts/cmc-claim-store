package uk.gov.hmcts.cmc.domain.constraints;

import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

public class BeanValidator {

    private BeanValidator() {
        // Utility class
    }

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    public static <T> Set<String> validate(T bean) {
        return getMessages(factory.getValidator().validate(bean));
    }

    private static <T> Set<String> getMessages(Set<ConstraintViolation<T>> response) {
        return response.stream()
            .map(BeanValidator::prepareMessage)
            .collect(Collectors.toSet());
    }

    private static String prepareMessage(ConstraintViolation<?> property) {
        if (!StringUtils.isEmpty(property.getPropertyPath().toString())) {
            return property.getPropertyPath() + " : " + property.getMessage();
        }

        return property.getMessage();
    }

}
