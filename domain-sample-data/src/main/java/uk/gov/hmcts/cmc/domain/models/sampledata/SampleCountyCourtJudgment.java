package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment.CountyCourtJudgmentBuilder;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;

public class SampleCountyCourtJudgment {

    private SampleCountyCourtJudgment() {
        super();
    }

    public static CountyCourtJudgmentBuilder builder() {
        return CountyCourtJudgment.builder()
            .ccjType(CountyCourtJudgmentType.DEFAULT)
            .paymentOption(PaymentOption.IMMEDIATELY);
    }
}
