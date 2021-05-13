package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.JacksonConfiguration;
import uk.gov.hmcts.cmc.claimstore.services.bankholidays.BankHolidays;
import uk.gov.hmcts.cmc.claimstore.services.bankholidays.BankHolidaysApi;
import uk.gov.hmcts.cmc.claimstore.services.bankholidays.NonWorkingDaysCollection;
import uk.gov.hmcts.cmc.claimstore.services.bankholidays.PublicHolidaysCollection;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;

import java.io.IOException;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.DayAssert.assertThat;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.toDate;

@RunWith(MockitoJUnitRunner.class)
public class ResponseDeadlineCalculatorTest {

    private static final int DAYS_FOR_RESPONSE = 14;
    private static final int DAYS_FOR_CLAIMANT_RESPONSE = 28;
    private static final int DAYS_FOR_SERVICE = 5;
    private static final int POSTPONE_BY = 14;

    @Mock
    private BankHolidaysApi bankHolidaysApi;

    @Mock
    private NonWorkingDaysCollection nonWorkingDaysCollection;

    private ResponseDeadlineCalculator calculator;

    @Before
    public void setUp() throws IOException {
        WorkingDayIndicator workingDayIndicator = new WorkingDayIndicator(
            new PublicHolidaysCollection(bankHolidaysApi),
            nonWorkingDaysCollection
        );

        when(bankHolidaysApi.retrieveAll()).thenReturn(loadFixture());

        calculator = new ResponseDeadlineCalculator(
            workingDayIndicator, DAYS_FOR_SERVICE, DAYS_FOR_RESPONSE, POSTPONE_BY, DAYS_FOR_CLAIMANT_RESPONSE
        );
    }

    @Test
    public void calculateResponseDeadlineWhenResponseDateOnSaturdayThenReturnFirstWorkingDay() {
        LocalDate responseDeadline = calculator.calculateResponseDeadline(toDate("2017-04-24"));

        assertThat(responseDeadline).isWeekday().isTheSame(toDate("2017-05-15"));
    }

    @Test
    public void calculateResponseDateOnSaturdayBeforeBankHolidayThenReturnFirstWorkingDay() {
        LocalDate issueDate = toDate("2017-05-08");
        LocalDate responseDeadline = calculator.calculateResponseDeadline(issueDate);

        assertThat(responseDeadline).isWeekday().isTheSame(toDate("2017-05-30"));
    }

    @Test
    public void calculateResponseDeadlineWhenResponseDateOnGoodFridayThenReturnFirstWorkingDay() {
        LocalDate mondayBeforeGoodFriday = toDate("2017-03-26");
        LocalDate responseDeadline = calculator.calculateResponseDeadline(mondayBeforeGoodFriday);

        assertThat(responseDeadline).isWeekday().isTheSame(toDate("2017-04-18"));
    }

    @Test
    public void calculateWhenDeadlineIsOnBankHolidaysAndPostponedDeadlineIsOnWorkingDay() {

        LocalDate issuedOn = toDate("2017-05-08");
        LocalDate responseDeadline = calculator.calculateResponseDeadline(issuedOn);
        LocalDate postponedDeadline = calculator.calculatePostponedResponseDeadline(issuedOn);
        LocalDate expectedDeadlineDate = toDate("2017-05-30"); // Tue as Mon was Bank holidays
        LocalDate expectedPostponedDate = toDate("2017-06-12"); // Mon as we calculate it based on issue date

        assertThat(responseDeadline).isWeekday().isTheSame(expectedDeadlineDate);
        assertThat(postponedDeadline).isWeekday().isTheSame(expectedPostponedDate);
    }

    @Test
    public void calculateWhenDeadlineIsWorkingDayAndPostponedDeadlineIsOnWorkingDay() {
        LocalDate issuedOn = toDate("2017-09-18");
        LocalDate expectedDeadlineDate = toDate("2017-10-09");
        LocalDate expectedPostponedDate = toDate("2017-10-23");

        LocalDate responseDeadline = calculator.calculateResponseDeadline(issuedOn);
        LocalDate postponedDeadline = calculator.calculatePostponedResponseDeadline(issuedOn);

        assertThat(responseDeadline).isWeekday().isTheSame(expectedDeadlineDate);
        assertThat(postponedDeadline).isWeekday().isTheSame(expectedPostponedDate);
    }

    @Test
    public void calculateWhenDeadlineIsOnWeekDayAsWellAsPostponedDeadline() {
        LocalDate issuedOn = toDate("2017-09-15");
        LocalDate expectedDeadlineDate = toDate("2017-10-04");
        LocalDate expectedPostponedDate = toDate("2017-10-18");

        LocalDate responseDeadline = calculator.calculateResponseDeadline(issuedOn);
        LocalDate postponedDeadline = calculator.calculatePostponedResponseDeadline(issuedOn);

        assertThat(responseDeadline).isWeekday().isTheSame(expectedDeadlineDate);
        assertThat(postponedDeadline).isWeekday().isTheSame(expectedPostponedDate);
    }

    @Test
    public void calculateWhenDeadlineIsOnNonWorkingDay() {
        LocalDate issuedOn = toDate("2017-09-15");
        LocalDate expectedDeadlineDate = toDate("2017-10-09");
        LocalDate expectedPostponedDate = toDate("2017-10-18");

        when(nonWorkingDaysCollection.contains(any(LocalDate.class)))
            .thenReturn(true, true, true, false);

        LocalDate responseDeadline = calculator.calculateResponseDeadline(issuedOn);
        LocalDate postponedDeadline = calculator.calculatePostponedResponseDeadline(issuedOn);

        assertThat(responseDeadline).isWeekday().isTheSame(expectedDeadlineDate);
        assertThat(postponedDeadline).isWeekday().isTheSame(expectedPostponedDate);
    }

    @Test
    public void calculateWhenServiceDateIsOnNonWorkingDay() {
        LocalDate issuedOn = toDate("2017-09-15");
        LocalDate expectedServiceDate = toDate("2017-09-20");

        LocalDate serviceDate = calculator.calculateServiceDate(issuedOn);

        assertThat(serviceDate).isTheSame(expectedServiceDate);
    }

    /**
     * The fixture was taken from the real bank holidays API.
     */
    private static BankHolidays loadFixture() throws IOException {
        String input = new ResourceReader().read("/bank-holidays.json");
        return new JacksonConfiguration().objectMapper().readValue(input, BankHolidays.class);
    }
}
