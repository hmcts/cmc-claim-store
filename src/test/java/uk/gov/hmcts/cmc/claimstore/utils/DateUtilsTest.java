package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DateUtilsTest {

    @Test
    public void startOfDay() {
        LocalDate localDate = LocalDate.of(2019, 7, 8);
        LocalDateTime dateTime = DateUtils.startOfDay(localDate);
        Assertions.assertEquals(localDate, dateTime.toLocalDate());
        Assertions.assertEquals(0, dateTime.getHour());
        Assertions.assertEquals(0, dateTime.getMinute());
        Assertions.assertEquals(0, dateTime.getSecond());
    }

    @Test
    public void endOfDay() {
        LocalDate localDate = LocalDate.of(2019, 7, 8);
        LocalDateTime dateTime = DateUtils.endOfDay(localDate);
        Assertions.assertEquals(localDate, dateTime.toLocalDate());
        Assertions.assertEquals(23, dateTime.getHour());
        Assertions.assertEquals(59, dateTime.getMinute());
        Assertions.assertEquals(59, dateTime.getSecond());
    }

    @Test
    public void startOfDayShouldNotAcceptNull() {
        assertThrows(NullPointerException.class, () -> {
            DateUtils.startOfDay(null);
        });
    }

    @Test
    public void endOfDayShouldNotAcceptNull() {
        assertThrows(NullPointerException.class, () -> {
            DateUtils.endOfDay(null);
        });
    }
}
