package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class ClaimDeadlineServiceTest {

    private final ClaimDeadlineService service = new ClaimDeadlineService();

    @Test
    public void isPastDeadlineShouldReturnFalseWhenTimeIsBefore4PMOnDeadlineDay() {
        LocalDateTime now = LocalDate.now().atTime(15, 59, 59, 999);
        LocalDate responseDeadline = LocalDate.now();
        assertThat(service.isPastDeadline(now, responseDeadline)).isFalse();
    }

    @Test
    public void isPastDeadlineShouldReturnTrueWhenTimeIs4PMOnDeadlineDay() {
        LocalDateTime now = LocalDate.now().atTime(16, 0);
        LocalDate responseDeadline = LocalDate.now();
        assertThat(service.isPastDeadline(now, responseDeadline)).isTrue();
    }

    @Test
    public void isPastDeadlineShouldReturnTrueWhenTimeIsPast4PMOnDeadlineDay() {
        LocalDateTime now = LocalDate.now().atTime(16, 1);
        LocalDate responseDeadline = LocalDate.now();
        assertThat(service.isPastDeadline(now, responseDeadline)).isTrue();
    }

}
