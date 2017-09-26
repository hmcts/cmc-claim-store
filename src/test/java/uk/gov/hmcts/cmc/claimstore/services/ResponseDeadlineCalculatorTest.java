package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.clients.RestClient;
import uk.gov.hmcts.cmc.claimstore.config.JacksonConfiguration;
import uk.gov.hmcts.cmc.claimstore.processors.RestClientFactory;
import uk.gov.hmcts.cmc.claimstore.services.PublicHolidaysCollection.BankHolidays;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceReader;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.DayAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ResponseDeadlineCalculatorTest {

    private static final String BANK_HOLIDAY_API_URL = "bank-holidays-api";

    private static final int DAYS_FOR_RESPONSE = 14;
    private static final int DAYS_FOR_SERVICE = 5;
    private static final int POSTPONE_BY = 14;

    @Mock
    private RestClientFactory restClientFactory;

    @Mock
    private RestClient restClient;

    private ResponseDeadlineCalculator calculator;

    @Before
    public void setUp() throws IOException {
        when(restClientFactory.create(BANK_HOLIDAY_API_URL)).thenReturn(restClient);

        WorkingDayIndicator workingDayIndicator = new WorkingDayIndicator(
            new PublicHolidaysCollection(restClientFactory, BANK_HOLIDAY_API_URL)
        );

        when(restClient.get(
            eq(PublicHolidaysCollection.Endpoints.BANK_HOLIDAYS), any())).thenReturn(loadFixture());

        calculator = new ResponseDeadlineCalculator(
            workingDayIndicator, DAYS_FOR_SERVICE, DAYS_FOR_RESPONSE, POSTPONE_BY
        );
    }

    @Test
    public void calculateResponseDeadlineWhenNoBankHolidays() {
        LocalDate responseDeadline = calculator.calculateResponseDeadline(toDate("2017-05-08"));

        assertThat(responseDeadline).isWeekday().isTheSame(toDate("2017-05-30"));
    }

    @Test
    public void calculateResponseDeadlineWhenResponseDateOnSaturdayThenReturnFirstWorkingDay() {
        LocalDate responseDeadline = calculator.calculateResponseDeadline(toDate("2017-04-24"));

        assertThat(responseDeadline).isWeekday().isTheSame(toDate("2017-05-15"));
    }

    @Test
    public void calculateResponseDeadlineWhenResponseDateOnSaturdayBeforeBankHolidayThenReturnFirstWorkingDay() {
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
    public void calculatePostponedResponseDeadlineWhenNoBankHolidays() {

        LocalDate responseDeadline = calculator.calculateResponseDeadline(toDate("2017-05-08"));
        LocalDate postponedDeadline = calculator.calculatePostponedResponseDeadline(responseDeadline);

        assertThat(responseDeadline).isWeekday().isTheSame(toDate("2017-05-30"));
        assertThat(postponedDeadline).isWeekday().isTheSame(toDate("2017-06-13"));
    }

    @Test
    public void testCaseFromProductionOne() {
        LocalDate issuedOn = toDate("2017-09-18");
        LocalDate expectedIssueDate = toDate("2017-10-09");
        LocalDate expectedPostponedDate = toDate("2017-10-23");

        LocalDate responseDeadline = calculator.calculateResponseDeadline(issuedOn);
        LocalDate postponedDeadline = calculator.calculatePostponedResponseDeadline(responseDeadline);

        assertThat(responseDeadline).isWeekday().isTheSame(expectedIssueDate);
        assertThat(postponedDeadline).isWeekday().isTheSame(expectedPostponedDate);
    }

    @Test
    public void testCaseFromProductionTwo() {
        LocalDate issuedOn = toDate("2017-09-15");
        LocalDate expectedIssueDate = toDate("2017-10-04");
        LocalDate expectedPostponedDate = toDate("2017-10-18");

        LocalDate responseDeadline = calculator.calculateResponseDeadline(issuedOn);
        LocalDate postponedDeadline = calculator.calculatePostponedResponseDeadline(responseDeadline);

        assertThat(responseDeadline).isWeekday().isTheSame(expectedIssueDate);
        assertThat(postponedDeadline).isWeekday().isTheSame(expectedPostponedDate);
    }

    private static LocalDate toDate(String dateString) {
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * The fixture was taken from the real bank holidays API
     */
    private static BankHolidays loadFixture() throws IOException {
        String input = new ResourceReader().read("/bank-holidays.json");
        return new JacksonConfiguration().objectMapper().readValue(input, BankHolidays.class);
    }

}
