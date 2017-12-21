package uk.gov.hmcts.cmc.domain.models.response;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SampleHowMuchOwed;
import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.utils.BeanValidator.validate;

public class HowMuchOwedTest {

    @Test
    public void shouldBeSuccessfulValidationForHowMuchOwed() {
        //given
        HowMuchOwed howMuchOwed = SampleHowMuchOwed.validDefaults();
        //when
        Set<String> response = validate(howMuchOwed);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForNullExplanation() {
        //given
        HowMuchOwed howMuchOwed = SampleHowMuchOwed.builder()
            .withExplanation(null)
            .build();
        //when
        Set<String> errors = validate(howMuchOwed);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("explanation : may not be empty");
    }

    @Test
    public void shouldBeInvalidForTooLongExplanation() {
        //given
        HowMuchOwed howMuchOwed = SampleHowMuchOwed.builder()
            .withExplanation(StringUtils.repeat("a", 300))
            .build();
        //when
        Set<String> errors = validate(howMuchOwed);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("explanation : Explanation should not be longer than 255 characters");
    }

    @Test
    public void shouldReturnValidationMessageWhenAmountHasValueLessThanMinimum() {
        //given
        HowMuchOwed howMuchOwed = new HowMuchOwed(new BigDecimal("0.00"), "explanation");
        //when
        Set<String> errors = validate(howMuchOwed);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("amount : must be greater than or equal to 0.01");
    }
}
