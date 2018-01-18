package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Calculates response deadline from submission date time.
 */
@Component
public class ResponseDeadlineCalculator {

    private final int serviceDays;
    private final int timeForResponseInDays;
    private final int requestedAdditionalTimeInDays;

    public ResponseDeadlineCalculator(
        @Value("${dateCalculations.serviceDays}") int serviceDays,
        @Value("${dateCalculations.responseDays}") int timeForResponseInDays,
        @Value("${dateCalculations.requestedAdditionalTimeInDays}") int requestedAdditionalTimeInDays) {

        this.serviceDays = serviceDays;
        this.timeForResponseInDays = timeForResponseInDays;
        this.requestedAdditionalTimeInDays = requestedAdditionalTimeInDays;
    }

    /**
     * Calculates response deadline by date of issue.
     */
    public LocalDate calculateResponseDeadline(LocalDate issueDate) {
        return issueDate.plusDays((long) timeForResponseInDays + serviceDays);
    }

    /**
     * We have to calculate postponed response deadline based on issue date, not current response deadline
     * as there are some edge cases that will not be covered otherwise.
     */
    public LocalDate calculatePostponedResponseDeadline(LocalDate issueDate) {
        return issueDate.plusDays((long) timeForResponseInDays + serviceDays + requestedAdditionalTimeInDays);
    }
}
