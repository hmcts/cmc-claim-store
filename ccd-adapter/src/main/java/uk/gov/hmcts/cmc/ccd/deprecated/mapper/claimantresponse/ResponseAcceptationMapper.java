package uk.gov.hmcts.cmc.ccd.deprecated.mapper.claimantresponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDFormaliseOption;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDResponseAcceptation;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.response.PaymentIntentionMapperOld;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;

@Component
public class ResponseAcceptationMapper implements Mapper<CCDResponseAcceptation, ResponseAcceptation> {

    private final PaymentIntentionMapperOld paymentIntentionMapperOld;
    private final CourtDeterminationMapperOld courtDeterminationMapperOld;

    @Autowired
    public ResponseAcceptationMapper(PaymentIntentionMapperOld paymentIntentionMapperOld,
                                     CourtDeterminationMapperOld courtDeterminationMapperOld) {
        this.paymentIntentionMapperOld = paymentIntentionMapperOld;
        this.courtDeterminationMapperOld = courtDeterminationMapperOld;
    }

    @Override
    public CCDResponseAcceptation to(ResponseAcceptation responseAcceptation) {
        CCDResponseAcceptation.CCDResponseAcceptationBuilder builder = CCDResponseAcceptation.builder();

        responseAcceptation.getFormaliseOption()
            .map(FormaliseOption::name)
            .map(CCDFormaliseOption::valueOf)
            .ifPresent(builder::formaliseOption);

        responseAcceptation.getAmountPaid().ifPresent(builder::amountPaid);

        responseAcceptation.getClaimantPaymentIntention().ifPresent(
            paymentIntention -> builder.claimantPaymentIntention(paymentIntentionMapperOld.to(paymentIntention))
        );

        responseAcceptation.getCourtDetermination().ifPresent(
            courtDetermination -> builder.courtDetermination(courtDeterminationMapperOld.to(courtDetermination))
        );

        return builder.build();
    }

    @Override
    public ResponseAcceptation from(CCDResponseAcceptation ccdResponseAcceptation) {
        return ResponseAcceptation.builder()
            .amountPaid(ccdResponseAcceptation.getAmountPaid())
            .formaliseOption(FormaliseOption.valueOf(ccdResponseAcceptation.getFormaliseOption().name()))
            .claimantPaymentIntention(paymentIntentionMapperOld
                .from(ccdResponseAcceptation.getClaimantPaymentIntention()))
            .courtDetermination(courtDeterminationMapperOld.from(ccdResponseAcceptation.getCourtDetermination()))
            .build();
    }
}
