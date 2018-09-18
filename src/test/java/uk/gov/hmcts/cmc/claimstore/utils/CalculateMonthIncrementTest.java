package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class CalculateMonthIncrementTest {

    @Test
    public void calculateMonthWhereStartDateBeforeTwentyEighth() {
        LocalDate startDate = LocalDate.of(2018, 10, 15);
        LocalDate calculateMonthDate = CalculateMonthIncrement.calculateMonthlyIncrement(startDate);
        assertThat(calculateMonthDate.toString()).isEqualTo("2018-11-15");
    }

    @Test
    public void calculateMonthWhereStartDateIsTwentyEighth() {
        LocalDate startDate = LocalDate.of(2018, 2, 28);
        LocalDate calculateMonthDate = CalculateMonthIncrement.calculateMonthlyIncrement(startDate);
        assertThat(calculateMonthDate.toString()).isEqualTo("2018-03-28");
    }

    @Test
    public void calculateMonthWhereStartDateIsThirtieth() {
        LocalDate startDate = LocalDate.of(2018, 3, 30);
        LocalDate calculateMonthDate = CalculateMonthIncrement.calculateMonthlyIncrement(startDate);
        assertThat(calculateMonthDate.toString()).isEqualTo("2018-04-30");
    }

    @Test
    public void calculateMonthWhereStartDateIsThirtyFirst() {
        LocalDate startDate = LocalDate.of(2018, 10, 31);
        LocalDate calculateMonthDate = CalculateMonthIncrement.calculateMonthlyIncrement(startDate);
        assertThat(calculateMonthDate.toString()).isEqualTo("2018-12-01");
    }

    @Test
    public void calculateMonthWhereStartDateIsTwentyNinthInJanuary() {
        LocalDate startDate = LocalDate.of(2018, 1, 29);
        LocalDate calculateMonthDate = CalculateMonthIncrement.calculateMonthlyIncrement(startDate);
        assertThat(calculateMonthDate.toString()).isEqualTo("2018-03-01");
    }

    @Test
    public void calculateMonthWhereStartDateIsThirtiethInJanuary() {
        LocalDate startDate = LocalDate.of(2018, 1, 30);
        LocalDate calculateMonthDate = CalculateMonthIncrement.calculateMonthlyIncrement(startDate);
        assertThat(calculateMonthDate.toString()).isEqualTo("2018-03-01");
    }

    @Test
    public void calculateMonthWhereStartDateIsThirtyFirstInJanuary() {
        LocalDate startDate = LocalDate.of(2018, 1, 31);
        LocalDate calculateMonthDate = CalculateMonthIncrement.calculateMonthlyIncrement(startDate);
        assertThat(calculateMonthDate.toString()).isEqualTo("2018-03-01");
    }

    @Test
    public void calculateMonthWhereStartDateIsTwentyNinthInLeapYear() {
        LocalDate startDate = LocalDate.of(2020, 1, 29);
        LocalDate calculateMonthDate = CalculateMonthIncrement.calculateMonthlyIncrement(startDate);
        assertThat(calculateMonthDate.toString()).isEqualTo("2020-02-29");
    }

    @Test
    public void calculateMonthWhereStartDateIsThirtiethInLeapYear() {
        LocalDate startDate = LocalDate.of(2020, 1, 30);
        LocalDate calculateMonthDate = CalculateMonthIncrement.calculateMonthlyIncrement(startDate);
        assertThat(calculateMonthDate.toString()).isEqualTo("2020-03-01");
    }

    @Test
    public void calculateMonthWhereStartDateIsThirtyFirstInLeapYear() {
        LocalDate startDate = LocalDate.of(2020, 1, 31);
        LocalDate calculateMonthDate = CalculateMonthIncrement.calculateMonthlyIncrement(startDate);
        assertThat(calculateMonthDate.toString()).isEqualTo("2020-03-01");
    }

    @Test
    public void calculateMonthWhereStartDateIsThirtyFirstIntoNextMonthWithThirtyOneDays() {
        LocalDate startDate = LocalDate.of(2018, 7, 31);
        LocalDate calculateMonthDate = CalculateMonthIncrement.calculateMonthlyIncrement(startDate);
        assertThat(calculateMonthDate.toString()).isEqualTo("2018-08-31");
    }

    @Test
    public void calculateMonthWhereStartDateIsThirtyFirstIntoNextMonthWithThirtyDays() {
        LocalDate startDate = LocalDate.of(2018,  8, 31);
        LocalDate calculateMonthDate = CalculateMonthIncrement.calculateMonthlyIncrement(startDate);
        assertThat(calculateMonthDate.toString()).isEqualTo("2018-10-01");
    }

    @Test
    public void calculateMonthGoingIntoTheFollowingYear() {
        LocalDate startDate = LocalDate.of(2018, 12, 31);
        LocalDate calculateMonthDate = CalculateMonthIncrement.calculateMonthlyIncrement(startDate);
        assertThat(calculateMonthDate.toString()).isEqualTo("2019-01-31");
    }

    @Test
    public void calculateMonthWhereAnUndefinedDateHasBeenPassed() {
        assertThat(CalculateMonthIncrement.calculateMonthlyIncrement(null)).isNull();
    }
}
