package uk.gov.hmcts.cmc.domain.models.offers.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MadeByEnumConverterTest {

    private MadeByEnumConverter converter;

    @BeforeEach
    public void beforeEachTest() {
        converter = new MadeByEnumConverter();
    }

    @Test
    public void shouldCorrectlySetLowerCaseStringAsCorrespondingEnumValue() {
        converter.setAsText("defendant");

        assertThat(converter.getValue()).isEqualTo(MadeBy.DEFENDANT);
    }

    @Test
    public void shouldThrowIllegalArgumentIfIncorrectEnumValueIsProvided() {
        assertThrows(IllegalArgumentException.class, () -> {
            converter.setAsText("abc");
        });
    }

}
