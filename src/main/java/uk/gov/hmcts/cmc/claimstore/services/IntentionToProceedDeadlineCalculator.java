package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class IntentionToProceedDeadlineCalculator {
    private final int intentionToProceedAdjustment;
    private final WorkingDayIndicator workingDayIndicator;

    public IntentionToProceedDeadlineCalculator(
        WorkingDayIndicator workingDayIndicator,
        @Value("${dateCalculations.intentionToProceedDeadlineInDays:33}") int intentionToProceedAdjustment
    ) {
        this.intentionToProceedAdjustment = intentionToProceedAdjustment;
        this.workingDayIndicator = workingDayIndicator;
    }

    public LocalDate calculateIntentionToProceedDeadline(LocalDate respondedAt) {
        LocalDate intentionToProceedDeadline = respondedAt.plusDays(this.intentionToProceedAdjustment);
        return workingDayIndicator.getNextWorkingDay(intentionToProceedDeadline);
    }

    public LocalDate calculateResponseDate(LocalDate intentionToProceedDeadline) {
        return workingDayIndicator.getPreviousWorkingDay(intentionToProceedDeadline)
            .minusDays(this.intentionToProceedAdjustment);
    }

}
