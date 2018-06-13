package uk.gov.hmcts.cmc.ccd.mapper.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentOption;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDFullAdmissionResponse;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.mapper.PartyMapper;
import uk.gov.hmcts.cmc.ccd.mapper.StatementOfTruthMapper;
import uk.gov.hmcts.cmc.ccd.mapper.ccj.RepaymentPlanMapper;
import uk.gov.hmcts.cmc.ccd.mapper.statementofmeans.StatementOfMeansMapper;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

@Component
public class FullAdmissionResponseMapper implements Mapper<CCDFullAdmissionResponse, FullAdmissionResponse> {

    private final PartyMapper partyMapper;
    private final RepaymentPlanMapper repaymentPlanMapper;
    private final StatementOfMeansMapper statementOfMeansMapper;
    private final StatementOfTruthMapper statementOfTruthMapper;

    @Autowired
    public FullAdmissionResponseMapper(
        PartyMapper partyMapper,
        RepaymentPlanMapper repaymentPlanMapper,
        StatementOfMeansMapper statementOfMeansMapper,
        StatementOfTruthMapper statementOfTruthMapper
    ) {
        this.partyMapper = partyMapper;
        this.repaymentPlanMapper = repaymentPlanMapper;
        this.statementOfMeansMapper = statementOfMeansMapper;
        this.statementOfTruthMapper = statementOfTruthMapper;
    }

    @Override
    public CCDFullAdmissionResponse to(FullAdmissionResponse fullAdmissionResponse) {
        CCDFullAdmissionResponse.CCDFullAdmissionResponseBuilder builder = CCDFullAdmissionResponse.builder()
            .moreTimeNeededOption(CCDYesNoOption.valueOf(fullAdmissionResponse.getMoreTimeNeeded().name()))
            .freeMediationOption(CCDYesNoOption.valueOf(
                fullAdmissionResponse.getFreeMediation().orElse(YesNoOption.NO).name())
            )
            .paymentDate(fullAdmissionResponse.getPaymentDate().orElse(null))
            .defendant(partyMapper.to(fullAdmissionResponse.getDefendant()))
            .paymentOption(CCDPaymentOption.valueOf(fullAdmissionResponse.getPaymentOption().name()));

        fullAdmissionResponse.getRepaymentPlan()
            .ifPresent(repaymentPlan -> builder.repaymentPlan(repaymentPlanMapper.to(repaymentPlan)));

        fullAdmissionResponse.getStatementOfMeans()
            .ifPresent(statementOfMeans -> builder.statementOfMeans(statementOfMeansMapper.to(statementOfMeans)));

        return builder.build();
    }

    @Override
    public FullAdmissionResponse from(CCDFullAdmissionResponse ccdFullAdmissionResponse) {
        CCDYesNoOption ccdFreeMediation = ccdFullAdmissionResponse.getFreeMediationOption();
        return new FullAdmissionResponse(
            ccdFreeMediation != null ? YesNoOption.valueOf(ccdFreeMediation.name()) : null,
            YesNoOption.valueOf(ccdFullAdmissionResponse.getMoreTimeNeededOption().name()),
            partyMapper.from(ccdFullAdmissionResponse.getDefendant()),
            statementOfTruthMapper.from(ccdFullAdmissionResponse.getStatementOfTruth()),
            PaymentOption.valueOf(ccdFullAdmissionResponse.getPaymentOption().name()),
            ccdFullAdmissionResponse.getPaymentDate(),
            repaymentPlanMapper.from(ccdFullAdmissionResponse.getRepaymentPlan()),
            statementOfMeansMapper.from(ccdFullAdmissionResponse.getStatementOfMeans())
        );
    }
}
