package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class OfferResponseDeadlineCalculator {

    private final WorkingDayIndicator workingDayIndicator;
    private final int offerMadeTimeForResponseInDays;
    private final int endOfBusinessDayHour;

    public OfferResponseDeadlineCalculator(
        final WorkingDayIndicator workingDayIndicator,
        @Value("${dateCalculations.offerMadeTimeForResponseInDays}") final int offerMadeTimeForResponseInDays,
        @Value("${dateCalculations.endOfBusinessDayHour}") final int endOfBusinessDayHour) {

        this.workingDayIndicator = workingDayIndicator;
        this.offerMadeTimeForResponseInDays = offerMadeTimeForResponseInDays;
        this.endOfBusinessDayHour = endOfBusinessDayHour;
    }

    public LocalDate calculateOfferResponseDeadline(final LocalDateTime offerSentAt) {

        LocalDate offerMadeOn = offerSentAt.toLocalDate();

        if (isTooLateForToday(offerSentAt)) {
            offerMadeOn = calculateFirstWorkingDayAfterOffset(offerMadeOn, 1);
        }

        return calculateFirstWorkingDayAfterOffset(offerMadeOn, offerMadeTimeForResponseInDays);
    }

    private LocalDate calculateFirstWorkingDayAfterOffset(final LocalDate date, final int offset) {
        LocalDate result = date.plusDays(offset);

        while (!workingDayIndicator.isWorkingDay(result)) {
            result = result.plusDays(1);
        }

        return result;
    }

    private boolean isTooLateForToday(final LocalDateTime dateTime) {
        return dateTime.getHour() >= endOfBusinessDayHour;
    }
}
