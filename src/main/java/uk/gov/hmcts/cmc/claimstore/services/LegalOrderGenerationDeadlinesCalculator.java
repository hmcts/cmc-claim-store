package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
public class LegalOrderGenerationDeadlinesCalculator {
    private static final int DAYS_FOR_RESPONSE = 42;

    private final WorkingDayIndicator workingDayIndicator;

    private final Clock clock;

    @Autowired
    public LegalOrderGenerationDeadlinesCalculator(
        Clock clock,
        WorkingDayIndicator workingDayIndicator) {
        this.clock = clock;
        this.workingDayIndicator = workingDayIndicator;
    }

    public LocalDate calculateOrderGenerationDeadlines() {
        long totalDays = DAYS_FOR_RESPONSE;
        LocalDate result = LocalDate.now(clock).plusDays(totalDays);

        while (!workingDayIndicator.isWorkingDay(result)) {
            result = result.plusDays(1);
        }

        return result;
    }
}
