package uk.gov.hmcts.cmccase.models;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmccase.utils.BeanValidator.validate;

public class InterestTest {

    @Test
    public void shouldHaveValidationMessagesWhenInterestAttributesAreNull() {
        //given
        Interest interest = new Interest(null, null, null);
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
        Interest interest = new Interest(Interest.InterestType.STANDARD, new BigDecimal(8), "reason");
        //when
        Set<String> errors = validate(interest);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldBeSuccessfulValidationForNoInterestType() {
        //given
        Interest interest = new Interest(Interest.InterestType.NO_INTEREST, null, "reason");
        //when
        Set<String> errors = validate(interest);
        //then
        assertThat(errors).isEmpty();
    }

}
