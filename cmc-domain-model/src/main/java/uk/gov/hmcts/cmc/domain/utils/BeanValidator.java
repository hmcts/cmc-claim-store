package uk.gov.hmcts.cmc.domain.utils;

import org.apache.commons.lang3.StringUtils;

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
            .map(BeanValidator::prepareMessage)
            .collect(Collectors.toSet());
    }

    private static String prepareMessage(ConstraintViolation property) {
        if (!StringUtils.isEmpty(property.getPropertyPath().toString())) {
            return property.getPropertyPath() + " : " + property.getMessage();
        }

        return property.getMessage();
    }
}
