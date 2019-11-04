package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    public LocalDate calculateResponseDate(LocalDateTime runDateTime) {
        //4pm cut off for court working days
        int adjustDays = runDateTime.getHour() >= 16 ? 0 : 1;
        LocalDate adjustedDate = runDateTime.toLocalDate().minusDays(adjustDays);

        return workingDayIndicator.getPreviousWorkingDay(adjustedDate).minusDays(this.intentionToProceedAdjustment);
    }

}
