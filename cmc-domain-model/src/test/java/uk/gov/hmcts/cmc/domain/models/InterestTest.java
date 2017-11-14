package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.utils.BeanValidator;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class InterestTest {

    @Test
    public void shouldHaveValidationMessagesWhenInterestAttributesAreNull() {
        //given
        Interest interest = new Interest(null, null, null);
        //when
        Set<String> errors = BeanValidator.validate(interest);
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
        Set<String> errors = BeanValidator.validate(interest);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldBeSuccessfulValidationForNoInterestType() {
        //given
        Interest interest = new Interest(Interest.InterestType.NO_INTEREST, null, "reason");
        //when
        Set<String> errors = BeanValidator.validate(interest);
        //then
        assertThat(errors).isEmpty();
    }

}
