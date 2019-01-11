package uk.gov.hmcts.cmc.ccd.util;

import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentIntention;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentOption;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentSchedule;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SampleCCDPaymentIntention {

    private SampleCCDPaymentIntention() {
        //Empty constructor
    }

    public static CCDPaymentIntention withInstalment() {
        return CCDPaymentIntention.builder().completionDate(LocalDate.now())
            .firstPaymentDate(LocalDate.now())
            .instalmentAmount(BigDecimal.TEN)
            .paymentSchedule(CCDPaymentSchedule.EVERY_MONTH)
            .paymentOption(CCDPaymentOption.INSTALMENTS)
            .build();
    }
}
