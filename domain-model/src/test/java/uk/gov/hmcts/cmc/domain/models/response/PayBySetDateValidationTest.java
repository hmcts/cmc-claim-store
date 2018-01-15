package uk.gov.hmcts.cmc.domain.models.response;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePayBySetDate;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.utils.BeanValidator.validate;

public class PayBySetDateValidationTest {

    @Test
    public void shouldPassForValidSample() {
        //given
        PayBySetDate payBySetDate = SamplePayBySetDate.validDefaults();
        //when
        Set<String> response = validate(payBySetDate);
        //then
        assertThat(response).isEmpty();
    }

    @Test
    public void shouldFailWhenExplanationEmpty() {
        //given
        PayBySetDate payBySetDate = SamplePayBySetDate.builder()
            .withExplanation("")
            .build();
        //when
        Set<String> errors = validate(payBySetDate);
        //then
        assertThat(errors)
            .containsExactly("explanation : may not be empty");
    }

    @Test
    public void shouldFailWhenExplanationTooLong() {
        //given
        PayBySetDate payBySetDate = SamplePayBySetDate.builder()
            .withExplanation(StringUtils.repeat("a", 99001))
            .build();
        //when
        Set<String> errors = validate(payBySetDate);
        //then
        assertThat(errors)
            .containsExactly("explanation : size must be between 0 and 99000");
    }

    @Test
    public void shouldFailWhenDateIsInThePast() {
        //given
        PayBySetDate payBySetDate = SamplePayBySetDate.builder()
            .withPastDate(LocalDate.now().minusDays(2))
            .build();
        //when
        Set<String> errors = validate(payBySetDate);
        //then
        assertThat(errors)
            .containsExactly("paymentDate : is in the past");
    }
}
