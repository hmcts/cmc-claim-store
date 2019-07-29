package uk.gov.hmcts.cmc.ccd.adapter.mapper.ccj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentSchedule;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgmentType;
import uk.gov.hmcts.cmc.ccd.adapter.mapper.MoneyMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.CCDPaymentOption.valueOf;

@Component
public class CountyCourtJudgmentMapper {

    private final MoneyMapper moneyMapper;

    @Autowired
    public CountyCourtJudgmentMapper(MoneyMapper moneyMapper) {
        this.moneyMapper = moneyMapper;
    }

    public CCDCountyCourtJudgment to(Claim claim) {

        if (claim == null || claim.getCountyCourtJudgment() == null) {
            return null;
        }

        CCDCountyCourtJudgment.CCDCountyCourtJudgmentBuilder builder = CCDCountyCourtJudgment.builder();

        CountyCourtJudgment countyCourtJudgment = claim.getCountyCourtJudgment();
        countyCourtJudgment.getDefendantDateOfBirth().ifPresent(builder::defendantDateOfBirth);
        countyCourtJudgment.getPaidAmount().map(moneyMapper::to).ifPresent(builder::paidAmount);
        builder.paymentOption(valueOf(countyCourtJudgment.getPaymentOption().name()));
        countyCourtJudgment.getRepaymentPlan().ifPresent(repaymentPlan -> {
            builder.repaymentPlanFirstPaymentDate(repaymentPlan.getFirstPaymentDate());
            builder.repaymentPlanInstalmentAmount(moneyMapper.to(repaymentPlan.getInstalmentAmount()));
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
            builder.type(CCDCountyCourtJudgmentType.valueOf(ccjType.name()))
        );

        builder.requestedDate(claim.getCountyCourtJudgmentRequestedAt());

        return builder.build();
    }

    public void from(CCDCountyCourtJudgment ccdCountyCourtJudgment, Claim.ClaimBuilder claimBuilder) {

        if (ccdCountyCourtJudgment == null) {
            return;
        }

        CountyCourtJudgment.CountyCourtJudgmentBuilder ccjBuilder = CountyCourtJudgment.builder()
            .defendantDateOfBirth(ccdCountyCourtJudgment.getDefendantDateOfBirth())
            .paidAmount(moneyMapper.from(ccdCountyCourtJudgment.getPaidAmount()))
            .payBySetDate(ccdCountyCourtJudgment.getPayBySetDate());

        if (ccdCountyCourtJudgment.getPaymentOption() != null) {
            ccjBuilder.paymentOption(PaymentOption.valueOf(ccdCountyCourtJudgment.getPaymentOption().name()));
        }

        if (ccdCountyCourtJudgment.getType() != null) {
            ccjBuilder.ccjType(CountyCourtJudgmentType.valueOf(ccdCountyCourtJudgment.getType().name()));
        }

        if (ccdCountyCourtJudgment.getRepaymentPlanFirstPaymentDate() != null
            && ccdCountyCourtJudgment.getRepaymentPlanPaymentSchedule() != null) {

            ccjBuilder.repaymentPlan(
                RepaymentPlan.builder()
                    .paymentLength(ccdCountyCourtJudgment.getRepaymentPlanPaymentLength())
                    .instalmentAmount(moneyMapper.from(ccdCountyCourtJudgment.getRepaymentPlanInstalmentAmount()))
                    .firstPaymentDate(ccdCountyCourtJudgment.getRepaymentPlanFirstPaymentDate())
                    .completionDate(ccdCountyCourtJudgment.getRepaymentPlanCompletionDate())
                    .paymentSchedule(
                        PaymentSchedule.valueOf(ccdCountyCourtJudgment.getRepaymentPlanPaymentSchedule().name()))
                    .build());
        }

        Optional.ofNullable(ccdCountyCourtJudgment.getStatementOfTruthSignerName()).ifPresent(sotSignerName ->
            ccjBuilder.statementOfTruth(
                StatementOfTruth.builder()
                    .signerName(ccdCountyCourtJudgment.getStatementOfTruthSignerName())
                    .signerRole(ccdCountyCourtJudgment.getStatementOfTruthSignerRole()).build())
        );

        claimBuilder.countyCourtJudgment(ccjBuilder.build());
        claimBuilder.countyCourtJudgmentRequestedAt(ccdCountyCourtJudgment.getRequestedDate());
    }
}
