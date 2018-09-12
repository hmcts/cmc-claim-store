package uk.gov.hmcts.cmc.claimstore.utils;

import java.time.LocalDate;

public final class CalculateMonthIncrement {

    private CalculateMonthIncrement(){
        //Static calculate month increment class, no instances required
    }

    public static LocalDate calculateMonthlyIncrement(LocalDate startDate) {
        if (startDate == null) {
            return null;
        }
        LocalDate futureMonth = startDate.plusMonths(1);
        return startDate.getDayOfMonth() != futureMonth.getDayOfMonth()
            && futureMonth.getDayOfMonth() == futureMonth.lengthOfMonth() ? futureMonth.plusDays(1) : futureMonth;
    }
}
