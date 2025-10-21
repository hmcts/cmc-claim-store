package uk.gov.hmcts.cmc.ccd.sample.data;

import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentIntention;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentOption;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentSchedule;

import java.time.LocalDate;

public class SampleCCDPaymentIntention {

    private SampleCCDPaymentIntention() {
        //Empty constructor
    }

    public static CCDPaymentIntention withInstalment() {
        return CCDPaymentIntention.builder().completionDate(LocalDate.now())
            .firstPaymentDate(LocalDate.now())
            .instalmentAmount("1000")
            .paymentSchedule(CCDPaymentSchedule.EVERY_MONTH)
            .paymentOption(CCDPaymentOption.INSTALMENTS)
            .completionDate(LocalDate.now().plusMonths(3))
            .paymentLength("3 months")
            .build();
    }
}
