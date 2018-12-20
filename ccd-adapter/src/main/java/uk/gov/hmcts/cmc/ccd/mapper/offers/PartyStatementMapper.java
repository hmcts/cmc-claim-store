package uk.gov.hmcts.cmc.ccd.mapper.offers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDMadeBy;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDPartyStatement;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDStatementType;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.mapper.PaymentIntentionMapper;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;

@Component
public class PartyStatementMapper implements Mapper<CCDPartyStatement, PartyStatement> {

    public PartyStatementMapper() {
        this.paymentIntentionMapper = new PaymentIntentionMapper();
    }

    private final PaymentIntentionMapper paymentIntentionMapper;

    //TODO change this constructor with autowire
    /*    public PartyStatementMapper(PaymentIntentionMapper paymentIntentionMapper) {
            this.paymentIntentionMapper = paymentIntentionMapper;
        }*/



    @Override
    public CCDPartyStatement to(PartyStatement partyStatement) {
        CCDPartyStatement.CCDPartyStatementBuilder builder = CCDPartyStatement.builder();
        builder.type(CCDStatementType.valueOf(partyStatement.getType().name()));
        builder.madeBy(CCDMadeBy.valueOf(partyStatement.getMadeBy().name()));
        partyStatement.getOffer().ifPresent(offer -> {
            builder.offerContent(offer.getContent());
            builder.offerCompletionDate(offer.getCompletionDate());
            offer.getPaymentIntention().ifPresent(paymentIntention ->
                builder.paymentIntention(paymentIntentionMapper.to(paymentIntention))
            );
        });

        return builder.build();
    }

    @Override
    public PartyStatement from(CCDPartyStatement ccdPartyStatement) {

        Offer offer = Offer.builder()
            .content(ccdPartyStatement.getOfferContent())
            .completionDate(ccdPartyStatement.getOfferCompletionDate())
            .paymentIntention(paymentIntentionMapper.from(ccdPartyStatement.getPaymentIntention()))
            .build();

        return PartyStatement.builder()
            .madeBy(MadeBy.valueOf(ccdPartyStatement.getMadeBy().name()))
            .type(StatementType.valueOf(ccdPartyStatement.getType().name()))
            .offer(offer)
            .build();

    }
}
