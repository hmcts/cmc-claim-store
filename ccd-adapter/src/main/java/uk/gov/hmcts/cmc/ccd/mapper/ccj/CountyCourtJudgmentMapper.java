package uk.gov.hmcts.cmc.ccd.mapper.ccj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.mapper.StatementOfTruthMapper;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;

import static uk.gov.hmcts.cmc.ccd.domain.ccj.CCDPaymentOption.valueOf;

@Component
public class CountyCourtJudgmentMapper implements Mapper<CCDCountyCourtJudgment, CountyCourtJudgment> {

    private final StatementOfTruthMapper statementOfTruthMapper;
    private final RepaymentPlanMapper repaymentPlanMapper;

    @Autowired
    public CountyCourtJudgmentMapper(
        StatementOfTruthMapper statementOfTruthMapper,
        RepaymentPlanMapper repaymentPlanMapper) {

        this.statementOfTruthMapper = statementOfTruthMapper;
        this.repaymentPlanMapper = repaymentPlanMapper;
    }

    @Override
    public CCDCountyCourtJudgment to(CountyCourtJudgment countyCourtJudgment) {

        CCDCountyCourtJudgment.CCDCountyCourtJudgmentBuilder builder = CCDCountyCourtJudgment.builder();

        countyCourtJudgment.getDefendantDateOfBirth().ifPresent(builder::defendantDateOfBirth);
        countyCourtJudgment.getPaidAmount().ifPresent(builder::paidAmount);
        builder.paymentOption(valueOf(countyCourtJudgment.getPaymentOption().name()));
        countyCourtJudgment.getRepaymentPlan().ifPresent(plan -> builder.repaymentPlan(repaymentPlanMapper.to(plan)));
        countyCourtJudgment.getPayBySetDate().ifPresent(builder::payBySetDate);
        countyCourtJudgment.getStatementOfTruth()
            .ifPresent(statementOfTruth -> builder.statementOfTruth(statementOfTruthMapper.to(statementOfTruth)));

        return builder.build();
    }

    @Override
    public CountyCourtJudgment from(CCDCountyCourtJudgment ccdCountyCourtJudgment) {

        return new CountyCourtJudgment(
            ccdCountyCourtJudgment.getDefendantDateOfBirth(),
            PaymentOption.valueOf(ccdCountyCourtJudgment.getPaymentOption().name()),
            ccdCountyCourtJudgment.getPaidAmount(),
            repaymentPlanMapper.from(ccdCountyCourtJudgment.getRepaymentPlan()),
            ccdCountyCourtJudgment.getPayBySetDate(),
            statementOfTruthMapper.from(ccdCountyCourtJudgment.getStatementOfTruth())
        );
    }
}
