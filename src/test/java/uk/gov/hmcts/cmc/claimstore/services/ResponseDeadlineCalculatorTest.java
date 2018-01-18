package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.cmc.claimstore.utils.DayAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ResponseDeadlineCalculatorTest {

    private static final int DAYS_FOR_RESPONSE = 14;
    private static final int DAYS_FOR_SERVICE = 5;
    private static final int POSTPONE_BY = 14;

    private ResponseDeadlineCalculator calculator;

    @Before
    public void setUp() {
        calculator = new ResponseDeadlineCalculator(DAYS_FOR_SERVICE, DAYS_FOR_RESPONSE, POSTPONE_BY);
    }

    @Test
    public void calculateResponseDeadline() {
        LocalDate issuedOn = now();
        LocalDate expectedDeadlineDate = issuedOn.plusDays(DAYS_FOR_RESPONSE + DAYS_FOR_SERVICE);

        LocalDate responseDeadline = calculator.calculateResponseDeadline(issuedOn);

        assertThat(responseDeadline).isTheSame(expectedDeadlineDate);
    }

    @Test
    public void calculatePostponedResponseDeadline() {
        LocalDate issuedOn = now();
        LocalDate expectedPostponedDate = issuedOn.plusDays(DAYS_FOR_RESPONSE + DAYS_FOR_SERVICE + POSTPONE_BY);

        LocalDate postponedDeadline = calculator.calculatePostponedResponseDeadline(issuedOn);

        assertThat(postponedDeadline).isTheSame(expectedPostponedDate);
    }

}
