package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.Test;

import java.util.Set;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class CourtOrderTest {
    public static CourtOrder.CourtOrderBuilder newSampleOfCourtOrderBuilder() {
        return CourtOrder.builder()
            .monthlyInstalmentAmount(ONE)
            .amountOwed(TEN)
            .claimNumber("My claim no");
    }

    @Test
    public void shouldBeSuccessfulValidationForCorrectCourtOrder() {
        //given
        CourtOrder courtOrder = newSampleOfCourtOrderBuilder().build();
        //when
        Set<String> response = validate(courtOrder);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForAllNullFields() {
        //given
        CourtOrder courtOrder = CourtOrder.builder().build();
        //when
        Set<String> errors = validate(courtOrder);
        //then
        assertThat(errors)
            .hasSize(3);
    }

    @Test
    public void shouldBeInvalidForNullAmountOwed() {
        //given
        CourtOrder courtOrder = CourtOrder.builder()
            .monthlyInstalmentAmount(ONE)
            .claimNumber("My claim no")
            .build();
        //when
        Set<String> errors = validate(courtOrder);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("amountOwed : may not be null");
    }

    @Test
    public void shouldBeInvalidForAmountOwedWithMoreThanTwoFractions() {
        //given
        CourtOrder courtOrder = CourtOrder.builder()
            .monthlyInstalmentAmount(ONE)
            .amountOwed(valueOf(1.123f))
            .claimNumber("My claim no")
            .build();
        //when
        Set<String> errors = validate(courtOrder);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("amountOwed : can not be more than 2 fractions");
    }

    @Test
    public void shouldBeInvalidForAmountLessThanOnePound() {
        //given
        CourtOrder courtOrder = CourtOrder.builder()
            .monthlyInstalmentAmount(valueOf(0.99))
            .amountOwed(valueOf(0.99))
            .claimNumber("My claim no")
            .build();
        //when
        Set<String> errors = validate(courtOrder);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("amountOwed : must be greater than or equal to 1.00");
    }

    @Test
    public void shouldBeInvalidForNullMonthlyInstalmentAmount() {
        //given
        CourtOrder courtOrder = CourtOrder.builder()
            .amountOwed(TEN)
            .claimNumber("My claim no")
            .build();
        //when
        Set<String> errors = validate(courtOrder);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("monthlyInstalmentAmount : may not be null");
    }

    @Test
    public void shouldBeInvalidForMontlyInstalmentAmountWithMoreThanTwoFractions() {
        //given
        CourtOrder courtOrder = CourtOrder.builder()
            .monthlyInstalmentAmount(valueOf(0.123f))
            .amountOwed(TEN)
            .claimNumber("My claim no")
            .build();
        //when
        Set<String> errors = validate(courtOrder);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("monthlyInstalmentAmount : can not be more than 2 fractions");
    }

    @Test
    public void shouldBeInvalidForNullClaimNumber() {
        //given
        CourtOrder courtOrder = CourtOrder.builder()
            .monthlyInstalmentAmount(ONE)
            .amountOwed(TEN)
            .build();
        //when
        Set<String> errors = validate(courtOrder);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("claimNumber : may not be empty");
    }

    @Test
    public void shouldBeInvalidForBlankClaimNumber() {
        //given
        CourtOrder courtOrder = CourtOrder.builder()
            .monthlyInstalmentAmount(ONE)
            .amountOwed(TEN)
            .claimNumber("")
            .build();
        //when
        Set<String> errors = validate(courtOrder);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("claimNumber : may not be empty");
    }
}
