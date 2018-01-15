package uk.gov.hmcts.cmc.domain.models.response;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SampleHowMuchOwed;
import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.utils.BeanValidator.validate;

public class HowMuchOwedValidationTest {

    @Test
    public void shouldPassForValidSample() {
        //given
        HowMuchOwed howMuchOwed = SampleHowMuchOwed.validDefaults();
        //when
        Set<String> response = validate(howMuchOwed);
        //then
        assertThat(response).isEmpty();
    }

    @Test
    public void shouldFailWhenExplanationEmpty() {
        //given
        HowMuchOwed howMuchOwed = SampleHowMuchOwed.builder()
            .withExplanation("")
            .build();
        //when
        Set<String> errors = validate(howMuchOwed);
        //then
        assertThat(errors)
            .containsExactly("explanation : may not be empty");
    }

    @Test
    public void shouldFailWhenExplanationTooLong() {
        //given
        HowMuchOwed howMuchOwed = SampleHowMuchOwed.builder()
            .withExplanation(StringUtils.repeat("a", 99001))
            .build();
        //when
        Set<String> errors = validate(howMuchOwed);
        //then
        assertThat(errors)
            .containsExactly("explanation : size must be between 0 and 99000");
    }

    @Test
    public void shouldFailWhenAmountIsZero() {
        //given
        HowMuchOwed howMuchOwed = SampleHowMuchOwed.builder()
            .withAmount(BigDecimal.ZERO)
            .build();
        //when
        Set<String> errors = validate(howMuchOwed);
        //then
        assertThat(errors)
            .containsExactly("amount : must be greater than or equal to 0.01");
    }
}
