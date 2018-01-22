package uk.gov.hmcts.cmc.ccd.mapper.ccj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.mapper.StatementOfTruthMapper;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentOption;

import java.time.LocalDate;

import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ISO_DATE;
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

        countyCourtJudgment.getDefendantDateOfBirth().ifPresent(dob -> dob.format(ISO_DATE));
        countyCourtJudgment.getPaidAmount().ifPresent(builder::paidAmount);
        builder.paymentOption(valueOf(countyCourtJudgment.getPaymentOption().name()));
        countyCourtJudgment.getRepaymentPlan().ifPresent(plan -> builder.repaymentPlan(repaymentPlanMapper.to(plan)));
        countyCourtJudgment.getPayBySetDate().ifPresent(pbs -> builder.payBySetDate(pbs.format(ISO_DATE)));
        countyCourtJudgment.getStatementOfTruth()
            .ifPresent(statementOfTruth -> builder.statementOfTruth(statementOfTruthMapper.to(statementOfTruth)));

        return builder.build();
    }

    @Override
    public CountyCourtJudgment from(CCDCountyCourtJudgment ccdCountyCourtJudgment) {
        LocalDate defendantDateOfBirth = ccdCountyCourtJudgment.getDefendantDateOfBirth() != null
            ? parse(ccdCountyCourtJudgment.getDefendantDateOfBirth(), ISO_DATE) : null;

        LocalDate payBySetDate = ccdCountyCourtJudgment.getPayBySetDate() != null
            ? parse(ccdCountyCourtJudgment.getPayBySetDate(), ISO_DATE) : null;

        return new CountyCourtJudgment(
            defendantDateOfBirth,
            PaymentOption.valueOf(ccdCountyCourtJudgment.getPaymentOption().name()),
            ccdCountyCourtJudgment.getPaidAmount(),
            repaymentPlanMapper.from(ccdCountyCourtJudgment.getRepaymentPlan()),
            payBySetDate,
            statementOfTruthMapper.from(ccdCountyCourtJudgment.getStatementOfTruth())
        );
    }
}
