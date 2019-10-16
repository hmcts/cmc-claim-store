package uk.gov.hmcts.cmc.claimstore.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class IntentionToProceedService {
    private final int intentionToProceedDeadline;

    private final WorkingDayIndicator workingDayIndicator;

    public IntentionToProceedService(
        WorkingDayIndicator workingDayIndicator,
        @Value("${intention.to.proceed.deadline:33}") int intentionToProceedDeadline
    ) {
        this.workingDayIndicator = workingDayIndicator;
        this.intentionToProceedDeadline = intentionToProceedDeadline;
    }

    public LocalDate calculateIntentionToProceedDeadline(LocalDate respondedAt) {
        LocalDate intentionToProceedDeadline = respondedAt.plusDays(this.intentionToProceedDeadline);
        intentionToProceedDeadline = workingDayIndicator.getNextWorkingDay(intentionToProceedDeadline);

        return intentionToProceedDeadline;
    }
}
