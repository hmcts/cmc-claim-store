package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateUtilsTest {

    @Test
    public void startOfDay() {
        LocalDate localDate = LocalDate.of(2019, 7, 8);
        LocalDateTime dateTime = DateUtils.startOfDay(localDate);
        Assert.assertEquals(localDate, dateTime.toLocalDate());
        Assert.assertEquals(0, dateTime.getHour());
        Assert.assertEquals(0, dateTime.getMinute());
        Assert.assertEquals(0, dateTime.getSecond());
    }

    @Test
    public void endOfDay() {
        LocalDate localDate = LocalDate.of(2019, 7, 8);
        LocalDateTime dateTime = DateUtils.endOfDay(localDate);
        Assert.assertEquals(localDate, dateTime.toLocalDate());
        Assert.assertEquals(23, dateTime.getHour());
        Assert.assertEquals(59, dateTime.getMinute());
        Assert.assertEquals(59, dateTime.getSecond());
    }

    @Test(expected = NullPointerException.class)
    public void startOfDayShouldNotAcceptNull() {
        DateUtils.startOfDay(null);
    }

    @Test(expected = NullPointerException.class)
    public void endOfDayShouldNotAcceptNull() {
        DateUtils.endOfDay(null);
    }
}
