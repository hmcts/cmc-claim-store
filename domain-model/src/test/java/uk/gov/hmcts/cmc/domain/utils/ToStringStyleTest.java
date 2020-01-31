package uk.gov.hmcts.cmc.domain.utils;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class ToStringStyleTest {

    private final ToStringStyleExample toStringStyleExample = new ToStringStyleExample(
        "Example",
        Arrays.asList("Example1", "Example2")
    );

    private final String expectedOutput = "["
        + System.lineSeparator()
        + "  field1=Example"
        + System.lineSeparator()
        + "  fieldList=[Example1, Example2]"
        + System.lineSeparator()
        + "]";

    @Test
    public void ourStyleReturnsExpectedFormat() {
        assertThat(toStringStyleExample.toString()).isEqualTo(expectedOutput);
    }

    static class ToStringStyleExample {
        private final String field1;
        private final List<String> fieldList;

        ToStringStyleExample(String field1, List<String> fieldList) {
            this.field1 = field1;
            this.fieldList = fieldList;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, ourStyle());
        }
    }
}
