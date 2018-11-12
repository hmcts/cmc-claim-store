package uk.gov.hmcts.cmc.domain;

import org.apache.commons.lang3.builder.ToStringStyle;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SensitiveAwareToStringBuilder {

    private SensitiveAwareToStringBuilder() {
    }

    public static String toString(final Object object, final ToStringStyle style) {
        StringBuilder toString = new StringBuilder();
        List<Field> fields = getAllFields(new LinkedList<>(), object.getClass());
        for (Field field : fields) {
            String name = field.getName();
            Object value;
            try {
                if (!Modifier.isPublic(field.getModifiers())) {
                    field.setAccessible(true);
                }

                value = field.isAnnotationPresent(Sensitive.class)
                    ? getMaskedValue(field.get(object))
                    : field.get(object);

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
