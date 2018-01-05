package uk.gov.hmcts.cmc.domain.models.response;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SampleHowMuchPaid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.utils.BeanValidator.validate;

public class HowMuchPaidValidationTest {

    @Test
    public void shouldPassForValidSample() {
        //given
        HowMuchPaid howMuchPaid = SampleHowMuchPaid.validDefaults();
        //when
        Set<String> response = validate(howMuchPaid);
        //then
        assertThat(response).isEmpty();
    }

    @Test
    public void shouldFailWhenExplanationEmpty() {
        //given
        HowMuchPaid howMuchPaid = SampleHowMuchPaid.builder()
            .withExplanation("")
            .build();
        //when
        Set<String> errors = validate(howMuchPaid);
        //then
        assertThat(errors)
            .containsExactly("explanation : may not be empty");
    }

    @Test
    public void shouldFailWhenExplanationTooLong() {
        //given
        HowMuchPaid howMuchPaid = SampleHowMuchPaid.builder()
            .withExplanation(StringUtils.repeat("a", 99001))
            .build();
        //when
        Set<String> errors = validate(howMuchPaid);
        //then
        assertThat(errors)
            .containsExactly("explanation : size must be between 0 and 99000");
    }

    @Test
    public void shouldFailWhenAmountIsZero() {
        //given
        HowMuchPaid howMuchPaid = SampleHowMuchPaid.builder()
            .withAmount(BigDecimal.ZERO)
            .build();
        //when
        Set<String> errors = validate(howMuchPaid);
        //then
        assertThat(errors)
            .containsExactly("amount : must be greater than or equal to 0.01");
    }

    @Test
    public void shouldFailWhenDateIsInTheFuture() {
        //given
        HowMuchPaid howMuchPaid = SampleHowMuchPaid.builder()
            .withPastDate(LocalDate.now().plusDays(2))
            .build();
        //when
        Set<String> errors = validate(howMuchPaid);
        //then
        assertThat(errors)
            .containsExactly("pastDate : is in the future");
    }
}
