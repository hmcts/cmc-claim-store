package uk.gov.hmcts.cmc.ccd.mapper.claimantresponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDFormaliseOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseAcceptation;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.mapper.response.PaymentIntentionMapper;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;

@Component
public class ResponseAcceptationMapper implements Mapper<CCDResponseAcceptation, ResponseAcceptation> {

    private final PaymentIntentionMapper paymentIntentionMapper;
    private final CourtDeterminationMapper courtDeterminationMapper;

    @Autowired
    public ResponseAcceptationMapper(PaymentIntentionMapper paymentIntentionMapper,
                                     CourtDeterminationMapper courtDeterminationMapper) {
        this.paymentIntentionMapper = paymentIntentionMapper;
        this.courtDeterminationMapper = courtDeterminationMapper;
    }

    @Override
    public CCDResponseAcceptation to(ResponseAcceptation responseAcceptation) {
        CCDResponseAcceptation.CCDResponseAcceptationBuilder builder = CCDResponseAcceptation.builder();
        builder.amountPaid(responseAcceptation.getAmountPaid())
            .formaliseOption(CCDFormaliseOption.valueOf(responseAcceptation.getFormaliseOption().name()));

        responseAcceptation.getClaimantPaymentIntention()
            .ifPresent(paymentIntention -> builder.claimantPaymentIntention(paymentIntentionMapper.to(paymentIntention)));

        responseAcceptation.getCourtDetermination()
            .ifPresent(courtDetermination -> builder.courtDetermination(courtDeterminationMapper.to(courtDetermination)));

        return builder.build();
    }

    @Override
    public ResponseAcceptation from(CCDResponseAcceptation ccdResponseAcceptation) {
        return ResponseAcceptation.builder()
            .amountPaid(ccdResponseAcceptation.getAmountPaid())
            .formaliseOption(FormaliseOption.valueOf(ccdResponseAcceptation.getFormaliseOption().name()))
            .claimantPaymentIntention(paymentIntentionMapper.from(ccdResponseAcceptation.getClaimantPaymentIntention()))
            .courtDetermination(courtDeterminationMapper.from(ccdResponseAcceptation.getCourtDetermination()))
            .build();
    }
}
