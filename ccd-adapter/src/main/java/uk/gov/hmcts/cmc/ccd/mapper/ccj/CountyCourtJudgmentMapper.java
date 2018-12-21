package uk.gov.hmcts.cmc.ccd.mapper.ccj;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import static java.util.Optional.ofNullable;

@Component
public class CountyCourtJudgmentMapper implements Mapper<CCDCountyCourtJudgment, CountyCourtJudgment> {

    @Override
    public CCDCountyCourtJudgment to(CountyCourtJudgment countyCourtJudgment) {

        CCDCountyCourtJudgment.CCDCountyCourtJudgmentBuilder builder = CCDCountyCourtJudgment.builder();

        countyCourtJudgment.getDefendantDateOfBirth().ifPresent(builder::defendantDateOfBirth);
        countyCourtJudgment.getPaidAmount().ifPresent(builder::paidAmount);
        builder.paymentOption(countyCourtJudgment.getPaymentOption());
        countyCourtJudgment.getPayBySetDate().ifPresent(builder::payBySetDate);

        countyCourtJudgment.getRepaymentPlan().ifPresent(repaymentPlan -> {
            builder.repaymentPlanFirstPaymentDate(repaymentPlan.getFirstPaymentDate());
            builder.repaymentPlanInstalmentAmount(repaymentPlan.getInstalmentAmount());
            builder.repaymentPlanPaymentLength(repaymentPlan.getPaymentLength());
            builder.repaymentPlanCompletionDate(repaymentPlan.getCompletionDate());
            builder.repaymentPlanPaymentSchedule(repaymentPlan.getPaymentSchedule());
        });

        countyCourtJudgment.getStatementOfTruth().ifPresent(sot -> {
            builder.statementOfTruthSignerName(sot.getSignerName());
            builder.statementOfTruthSignerRole(sot.getSignerRole());
        });

        ofNullable(countyCourtJudgment.getCcjType()).ifPresent(builder::ccjType);

        return builder.build();
    }

    @Override
    public CountyCourtJudgment from(CCDCountyCourtJudgment ccdCountyCourtJudgment) {

        CountyCourtJudgment.CountyCourtJudgmentBuilder ccjBuilder = CountyCourtJudgment.builder();

        ofNullable(ccdCountyCourtJudgment.getDefendantDateOfBirth()).ifPresent(ccjBuilder::defendantDateOfBirth);
        ofNullable(ccdCountyCourtJudgment.getPaidAmount()).ifPresent(ccjBuilder::paidAmount);
        ofNullable(ccdCountyCourtJudgment.getPayBySetDate()).ifPresent(ccjBuilder::payBySetDate);
        ccjBuilder.paymentOption(ccdCountyCourtJudgment.getPaymentOption());
        ofNullable(ccdCountyCourtJudgment.getCcjType()).ifPresent(ccjBuilder::ccjType);

        if (ccdCountyCourtJudgment.getRepaymentPlanFirstPaymentDate() != null
            && ccdCountyCourtJudgment.getRepaymentPlanPaymentSchedule() != null) {

            ccjBuilder.repaymentPlan(RepaymentPlan.builder()
                .firstPaymentDate(ccdCountyCourtJudgment.getRepaymentPlanFirstPaymentDate())
                .instalmentAmount(ccdCountyCourtJudgment.getRepaymentPlanInstalmentAmount())
                .paymentSchedule(ccdCountyCourtJudgment.getRepaymentPlanPaymentSchedule())
                .completionDate(ccdCountyCourtJudgment.getRepaymentPlanCompletionDate())
                .paymentLength(ccdCountyCourtJudgment.getRepaymentPlanPaymentLength())
                .build());
        }

        if (!StringUtils.isBlank(ccdCountyCourtJudgment.getStatementOfTruthSignerName())
            && !StringUtils.isBlank(ccdCountyCourtJudgment.getStatementOfTruthSignerRole())) {

            ccjBuilder.statementOfTruth(StatementOfTruth.builder()
                .signerName(ccdCountyCourtJudgment.getStatementOfTruthSignerName())
                .signerRole(ccdCountyCourtJudgment.getStatementOfTruthSignerRole())
                .build());
        }

        return ccjBuilder.build();
    }
}
