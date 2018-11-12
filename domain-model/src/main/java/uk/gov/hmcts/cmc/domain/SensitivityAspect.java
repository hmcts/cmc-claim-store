package uk.gov.hmcts.cmc.domain;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.joining;

@Aspect
public class SensitivityAspect {
    private static final String DEFAULT_MASK = "[MASKED]";
    private static final String UNREADABLE = "[UNREADABLE]";
    private static final Predicate<Field> isSensitive = field -> field.isAnnotationPresent(Sensitive.class);

    @Pointcut("execution(String toString())")
    public void toStringCall() {
    }

    @Around("toStringCall()")
    public Object maskSensitiveFieldsOrDefault(ProceedingJoinPoint joinPoint) throws Throwable {
        final Object caller = joinPoint.getTarget();
        final List<Field> fields = getAllFields(new LinkedList<>(), caller.getClass());

        return fields.stream().anyMatch(isSensitive)
            ? maskSensitiveFields(caller, fields)
            : joinPoint.proceed();
    }

    private String maskSensitiveFields(Object caller, List<Field> fields) {
        return fields.stream()
            .peek(field -> field.setAccessible(true))
            .map(fieldAsString(caller))
            .collect(joining(", ", caller.getClass().getSimpleName() + " {", "}"));
    }

    private static Function<Field, String> fieldAsString(Object caller) {
        return field -> {
            String result = field.getName() + "=";
            try {
                result += field.isAnnotationPresent(Sensitive.class)
                    ? DEFAULT_MASK
                    : Objects.toString(field.get(caller));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                result += UNREADABLE;
            }
            return result;
        };
    }

    private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }
        return fields;
    }
}
