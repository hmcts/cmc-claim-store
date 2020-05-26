package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ResponseDeadlineCalculator {

    private final WorkingDayIndicator workingDayIndicator;
    private final int serviceDays;
    private final int timeForResponseInDays;
    private final int requestedAdditionalTimeInDays;
    private final int timeForClaimantResponseInDays;

    public ResponseDeadlineCalculator(
        WorkingDayIndicator workingDayIndicator,
        @Value("${dateCalculations.serviceDays}") int serviceDays,
        @Value("${dateCalculations.responseDays}") int timeForResponseInDays,
        @Value("${dateCalculations.requestedAdditionalTimeInDays}") int requestedAdditionalTimeInDays,
        @Value("${dateCalculations.claimantResponseDays}") int timeForClaimantResponseInDays) {

        this.workingDayIndicator = workingDayIndicator;
        this.serviceDays = serviceDays;
        this.timeForResponseInDays = timeForResponseInDays;
        this.requestedAdditionalTimeInDays = requestedAdditionalTimeInDays;
        this.timeForClaimantResponseInDays = timeForClaimantResponseInDays;
    }

    public LocalDate calculateResponseDeadline(LocalDate issueDate) {
        return calculateFirstWorkingDayAfterOffset(issueDate, serviceDays + timeForResponseInDays);
    }

    /*
     * We have to calculate postponed response deadline based on issue date, not current response deadline
     * as there are some edge cases that will not be covered otherwise.
     */
    public LocalDate calculatePostponedResponseDeadline(LocalDate issueDate) {
        return calculateFirstWorkingDayAfterOffset(
            issueDate, serviceDays + timeForResponseInDays + requestedAdditionalTimeInDays
        );
    }

    LocalDate calculateClaimantResponseDeadline(LocalDate responseDate) {
        return calculateFirstWorkingDayAfterOffset(responseDate, serviceDays + timeForClaimantResponseInDays);
    }

    public LocalDate calculateServiceDate(LocalDate issueDate) {
        return calculateFirstWorkingDayAfterOffset(issueDate, serviceDays);
    }

    private LocalDate calculateFirstWorkingDayAfterOffset(LocalDate date, int offset) {
        LocalDate result = date.plusDays(offset);

        while (!workingDayIndicator.isWorkingDay(result)) {
            result = result.plusDays(1);
        }

        return result;
    }
}
