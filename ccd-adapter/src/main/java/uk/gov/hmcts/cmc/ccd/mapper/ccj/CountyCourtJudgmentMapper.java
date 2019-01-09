package uk.gov.hmcts.cmc.ccd.mapper.ccj;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentSchedule;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgmentType;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.CCDPaymentOption.valueOf;

@Component
public class CountyCourtJudgmentMapper implements Mapper<CCDCountyCourtJudgment, CountyCourtJudgment> {

    @Override
    public CCDCountyCourtJudgment to(CountyCourtJudgment countyCourtJudgment) {

        if (countyCourtJudgment == null) {
            return null;
        }

        CCDCountyCourtJudgment.CCDCountyCourtJudgmentBuilder builder = CCDCountyCourtJudgment.builder();

        countyCourtJudgment.getDefendantDateOfBirth().ifPresent(builder::defendantDateOfBirth);
        countyCourtJudgment.getPaidAmount().ifPresent(builder::paidAmount);
        builder.paymentOption(valueOf(countyCourtJudgment.getPaymentOption().name()));
        countyCourtJudgment.getRepaymentPlan().ifPresent(repaymentPlan -> {
            builder.repaymentPlanFirstPaymentDate(repaymentPlan.getFirstPaymentDate());
            builder.repaymentPlanInstalmentAmount(repaymentPlan.getInstalmentAmount());
            builder.repaymentPlanPaymentLength(repaymentPlan.getPaymentLength());
            builder.repaymentPlanPaymentSchedule(CCDPaymentSchedule.valueOf(repaymentPlan.getPaymentSchedule().name()));
            builder.repaymentPlanCompletionDate(repaymentPlan.getCompletionDate());
        });
        countyCourtJudgment.getPayBySetDate().ifPresent(builder::payBySetDate);
        countyCourtJudgment.getStatementOfTruth()
            .ifPresent(sot -> {
                builder.statementOfTruthSignerName(sot.getSignerName());
                builder.statementOfTruthSignerRole(sot.getSignerRole());
            });
        Optional.ofNullable(countyCourtJudgment.getCcjType()).ifPresent(ccjType ->
            builder.ccjType(CCDCountyCourtJudgmentType.valueOf(ccjType.name()))
        );

        return builder.build();
    }

    @Override
    public CountyCourtJudgment from(CCDCountyCourtJudgment ccdCountyCourtJudgment) {

        CountyCourtJudgment.CountyCourtJudgmentBuilder builder = CountyCourtJudgment.builder()
            .defendantDateOfBirth(ccdCountyCourtJudgment.getDefendantDateOfBirth())
            .paidAmount(ccdCountyCourtJudgment.getPaidAmount())
            .payBySetDate(ccdCountyCourtJudgment.getPayBySetDate());

        if (ccdCountyCourtJudgment.getPaymentOption() != null) {
            builder.paymentOption(PaymentOption.valueOf(ccdCountyCourtJudgment.getPaymentOption().name()));
        }

        if (ccdCountyCourtJudgment.getCcjType() != null) {
            builder.ccjType(CountyCourtJudgmentType.valueOf(ccdCountyCourtJudgment.getCcjType().name()));
        }

        if (ccdCountyCourtJudgment.getRepaymentPlanFirstPaymentDate() != null
            && ccdCountyCourtJudgment.getRepaymentPlanPaymentSchedule() != null) {

            builder.repaymentPlan(
                RepaymentPlan.builder()
                    .paymentLength(ccdCountyCourtJudgment.getRepaymentPlanPaymentLength())
                    .instalmentAmount(ccdCountyCourtJudgment.getRepaymentPlanInstalmentAmount())
                    .firstPaymentDate(ccdCountyCourtJudgment.getRepaymentPlanFirstPaymentDate())
                    .completionDate(ccdCountyCourtJudgment.getRepaymentPlanCompletionDate())
                    .paymentSchedule(
                        PaymentSchedule.valueOf(ccdCountyCourtJudgment.getRepaymentPlanPaymentSchedule().name()))
                    .build());
        }

        Optional.ofNullable(ccdCountyCourtJudgment.getStatementOfTruthSignerName()).ifPresent(sotSignerName ->
            builder.statementOfTruth(
                StatementOfTruth.builder()
                    .signerName(ccdCountyCourtJudgment.getStatementOfTruthSignerName())
                    .signerRole(ccdCountyCourtJudgment.getStatementOfTruthSignerRole()).build())
        );


        return builder.build();
    }
}
