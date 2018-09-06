package uk.gov.hmcts.cmc.ccd.mapper.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDFullAdmissionResponse;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.mapper.PartyMapper;
import uk.gov.hmcts.cmc.ccd.mapper.StatementOfTruthMapper;
import uk.gov.hmcts.cmc.ccd.mapper.statementofmeans.StatementOfMeansMapper;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

@Component
public class FullAdmissionResponseMapper implements Mapper<CCDFullAdmissionResponse, FullAdmissionResponse> {

    private final PartyMapper partyMapper;
    private final PaymentIntentionMapper paymentIntentionMapper;
    private final StatementOfMeansMapper statementOfMeansMapper;
    private final StatementOfTruthMapper statementOfTruthMapper;

    @Autowired
    public FullAdmissionResponseMapper(
        PartyMapper partyMapper,
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
        return new FullAdmissionResponse(
            ccdFreeMediation != null ? YesNoOption.valueOf(ccdFreeMediation.name()) : null,
            moreTimeNeeded != null ? YesNoOption.valueOf(moreTimeNeeded.name()) : null,
            partyMapper.from(ccdFullAdmissionResponse.getDefendant()),
            statementOfTruthMapper.from(ccdFullAdmissionResponse.getStatementOfTruth()),
            paymentIntentionMapper.from(ccdFullAdmissionResponse.getPaymentIntention()),
            statementOfMeansMapper.from(ccdFullAdmissionResponse.getStatementOfMeans())
        );
    }
}
