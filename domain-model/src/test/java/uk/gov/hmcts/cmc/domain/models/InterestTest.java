package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class InterestTest {

    @Test
    public void shouldHaveValidationMessagesWhenInterestAttributesAreNull() {
        //given
        Interest interest = SampleInterest.builder()
            .withType(null)
            .withRate(null)
            .withReason(null)
            .build();
        //when
        Set<String> errors = validate(interest);
        //then
        assertThat(errors)
            .hasSize(2)
            .contains(
                "type : may not be null",
                "rate : may not be null"
            );
    }

    @Test
    public void shouldBeSuccessfulValidationForInterestDate1() {
        //given
        Interest interest = SampleInterest.builder()
            .withType(Interest.InterestType.STANDARD)
            .withRate(new BigDecimal(8))
            .withReason("reason")
            .build();
        //when
        Set<String> errors = validate(interest);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldBeSuccessfulValidationForNoInterestType() {
        //given
        Interest interest = SampleInterest.builder()
            .withType(Interest.InterestType.NO_INTEREST)
            .withRate(null)
            .withReason(null)
            .build();
        //when
        Set<String> errors = validate(interest);
        //then
        assertThat(errors).isEmpty();
    }

}
