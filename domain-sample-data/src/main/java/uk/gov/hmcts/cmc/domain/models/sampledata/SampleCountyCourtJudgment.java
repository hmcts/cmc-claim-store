package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment.CountyCourtJudgmentBuilder;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;

import java.math.BigDecimal;

public class SampleCountyCourtJudgment {

    private SampleCountyCourtJudgment() {
        super();
    }

    public static CountyCourtJudgmentBuilder builder() {
        return CountyCourtJudgment.builder()
            .paidAmount(BigDecimal.ZERO)
            .paymentOption(PaymentOption.IMMEDIATELY);
    }
}
