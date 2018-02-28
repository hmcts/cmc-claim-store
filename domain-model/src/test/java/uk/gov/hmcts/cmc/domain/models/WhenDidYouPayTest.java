package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleWhenDidYouPay;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class WhenDidYouPayTest {

    @Test
    public void shouldBeSuccessfulValidationForWhenDidYouPay() {
        //given
        WhenDidYouPay whenDidYouPay = SampleWhenDidYouPay.validDefaults();
        //when
        Set<String> response = validate(whenDidYouPay);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForNullExplanation() {
        //given
        WhenDidYouPay whenDidYouPay = SampleWhenDidYouPay.builder()
            .withExplanation(null)
            .build();
        //when
        Set<String> errors = validate(whenDidYouPay);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("explanation : may not be empty");
    }

    @Test
    public void shouldHaveValidationMessagesWhenExplanationExceedsSizeLimint() {
        //given
        String explanation = new ResourceReader().read("/defence_exceeding_size_limit.text");

        WhenDidYouPay whenDidYouPay = SampleWhenDidYouPay.builder()
            .withExplanation(explanation)
            .build();
        //when
        Set<String> errors = validate(whenDidYouPay);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("explanation : size must be between 0 and 99000");
    }

    @Test
    public void shouldHaveValidationMessagesWhenExplanationIsEmpty() {
        //given
        WhenDidYouPay whenDidYouPay = SampleWhenDidYouPay.builder()
            .withExplanation("")
            .build();
        //when
        Set<String> errors = validate(whenDidYouPay);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("explanation : may not be empty");
    }

    @Test
    public void shouldHaveValidationMessagesWhenPaidDateIsInTheFuture() {
        //given
        WhenDidYouPay whenDidYouPay = SampleWhenDidYouPay.builder()
            .withExplanation("defence")
            .withPaidDate(LocalDate.now().plusYears(1))
            .build();
        //when
        Set<String> errors = validate(whenDidYouPay);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("paidDate : is in the future");

    }


    @Test
    public void shouldHaveValidationMessagesWhenPaidDateIsNull() {
        //given
        WhenDidYouPay whenDidYouPay = SampleWhenDidYouPay.builder()
            .withExplanation("defence")
            .withPaidDate(null)
            .build();
        //when
        Set<String> errors = validate(whenDidYouPay);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("paidDate : may not be null");

    }

    @Test
    public void shouldHaveNoErrorsWhenThereIsValidDateAndExplanation() {
        //given
        WhenDidYouPay whenDidYouPay = SampleWhenDidYouPay.builder()
            .withExplanation("paid by credit cars")
            .withPaidDate(LocalDate.now().minusDays(10))
            .build();
        //when
        Set<String> errors = validate(whenDidYouPay);
        //then
        assertThat(errors).hasSize(0);


    }
}
