package uk.gov.hmcts.cmc.domain;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Aspect
public class SensitivityAspect {

    @Pointcut("execution(String toString())")
    public void toStringCall() {
    }

    @Around("toStringCall()")
    public Object maskSensitiveFieldsOrDefault(ProceedingJoinPoint joinPoint) throws Throwable {
        final Object caller = joinPoint.getTarget();
        List<Field> fields = getAllFields(new LinkedList<>(), caller.getClass());
        if (fields.stream().map(field -> field.getAnnotationsByType(Sensitive.class)).noneMatch(Objects::nonNull)) {
            return joinPoint.proceed();
        }

        return maskSensitiveFields(caller, fields);
    }

    private String maskSensitiveFields(Object caller, List<Field> fields) {
        StringBuilder toString = new StringBuilder();
        for (Field field : fields) {
            String name = field.getName();
            Object value;
            try {
                if (!Modifier.isPublic(field.getModifiers())) {
                    field.setAccessible(true);
                }
                value = field.isAnnotationPresent(Sensitive.class)
                    ? getMaskedValue(field.get(caller))
                    : field.get(caller);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                value = "[exception thrown while accessing]";
            }
            toString.append(name).append("=").append(value).append(", ");
        }
        return "[" + toString.toString().replaceAll(",\\s*$", "") + "]";
    }

    private static String getMaskedValue(Object input) {
        char[] value = input.toString().toCharArray();
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < value.length; i++) {
            output.append("+");
        }
        return output.toString();
    }

    private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }
        return fields;
    }
}
