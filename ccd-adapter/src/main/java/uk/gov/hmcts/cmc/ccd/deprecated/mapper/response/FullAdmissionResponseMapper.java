package uk.gov.hmcts.cmc.ccd.deprecated.mapper.response;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDFullAdmissionResponse;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.StatementOfTruthMapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.statementofmeans.StatementOfMeansMapper;
import uk.gov.hmcts.cmc.ccd.mapper.DefendantMapper;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Optional;

//@Component
public class FullAdmissionResponseMapper implements Mapper<CCDFullAdmissionResponse, FullAdmissionResponse> {

    private final DefendantMapper partyMapper;
    private final PaymentIntentionMapper paymentIntentionMapper;
    private final StatementOfMeansMapper statementOfMeansMapper;
    private final StatementOfTruthMapper statementOfTruthMapper;

    @Autowired
    public FullAdmissionResponseMapper(
        DefendantMapper partyMapper,
        PaymentIntentionMapper paymentIntentionMapper,
        StatementOfMeansMapper statementOfMeansMapper,
        StatementOfTruthMapper statementOfTruthMapper
    ) {
        this.partyMapper = partyMapper;
        this.paymentIntentionMapper = paymentIntentionMapper;
        this.statementOfMeansMapper = statementOfMeansMapper;
        this.statementOfTruthMapper = statementOfTruthMapper;
    }

    @Override
    public CCDFullAdmissionResponse to(FullAdmissionResponse fullAdmissionResponse) {
        CCDFullAdmissionResponse.CCDFullAdmissionResponseBuilder builder = CCDFullAdmissionResponse.builder()
            .freeMediationOption(CCDYesNoOption.valueOf(
                fullAdmissionResponse.getFreeMediation().orElse(YesNoOption.NO).name())
            )
            .defendant(partyMapper.to(fullAdmissionResponse.getDefendant()))
            .paymentIntention(paymentIntentionMapper.to(fullAdmissionResponse.getPaymentIntention()));

        if (fullAdmissionResponse.getMoreTimeNeeded() != null) {
            builder.moreTimeNeededOption(CCDYesNoOption.valueOf(fullAdmissionResponse.getMoreTimeNeeded().name()));
        }

        fullAdmissionResponse.getStatementOfMeans()
            .ifPresent(statementOfMeans -> builder.statementOfMeans(statementOfMeansMapper.to(statementOfMeans)));

        fullAdmissionResponse.getStatementOfTruth()
            .ifPresent(statementOfTruth -> builder.statementOfTruth(statementOfTruthMapper.to(statementOfTruth)));

        return builder.build();
    }

    @Override
    public FullAdmissionResponse from(CCDFullAdmissionResponse ccdFullAdmissionResponse) {
        CCDYesNoOption ccdFreeMediation = ccdFullAdmissionResponse.getFreeMediationOption();
        CCDYesNoOption moreTimeNeeded = ccdFullAdmissionResponse.getMoreTimeNeededOption();
        return FullAdmissionResponse.builder()
            .freeMediation(YesNoOption.valueOf(Optional.ofNullable(ccdFreeMediation).orElse(CCDYesNoOption.NO).name()))
            .moreTimeNeeded(YesNoOption.valueOf(Optional.ofNullable(moreTimeNeeded).orElse(CCDYesNoOption.NO).name()))
            .defendant(partyMapper.from(ccdFullAdmissionResponse.getDefendant()))
            .statementOfTruth(statementOfTruthMapper.from(ccdFullAdmissionResponse.getStatementOfTruth()))
            .paymentIntention(paymentIntentionMapper.from(ccdFullAdmissionResponse.getPaymentIntention()))
            .statementOfMeans(statementOfMeansMapper.from(ccdFullAdmissionResponse.getStatementOfMeans()))
            .build();
    }
}
