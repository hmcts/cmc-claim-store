package uk.gov.hmcts.cmc.domain.models.offers.converters;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

import static org.assertj.core.api.Assertions.assertThat;

public class MadeByEnumConverterTest {

    private MadeByEnumConverter converter;

    @Before
    public void beforeEachTest() {
        converter = new MadeByEnumConverter();
    }

    @Test
    public void shouldCorrectlySetLowerCaseStringAsCorrespondingEnumValue() {
        converter.setAsText("defendant");

        assertThat(converter.getValue()).isEqualTo(MadeBy.DEFENDANT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentIfIncorrectEnumValueIsProvided() {
        converter.setAsText("abc");
    }

}
