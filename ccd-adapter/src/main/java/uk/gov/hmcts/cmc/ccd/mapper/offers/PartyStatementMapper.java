package uk.gov.hmcts.cmc.ccd.mapper.offers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDPartyStatement;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDMadeBy;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDStatementType;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.mapper.PaymentIntentionMapper;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;

import java.util.Objects;
import java.util.stream.Stream;

@Component
public class PartyStatementMapper implements Mapper<CCDPartyStatement, PartyStatement> {

    private final PaymentIntentionMapper paymentIntentionMapper;

    @Autowired
    public PartyStatementMapper(PaymentIntentionMapper paymentIntentionMapper) {
        this.paymentIntentionMapper = paymentIntentionMapper;
    }

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

        return PartyStatement.builder()
            .madeBy(MadeBy.valueOf(ccdPartyStatement.getMadeBy().name()))
            .type(StatementType.valueOf(ccdPartyStatement.getType().name()))
            .offer(buildOfferFromCCDPartyStatement(ccdPartyStatement))
            .build();

    }

    private Offer buildOfferFromCCDPartyStatement(CCDPartyStatement ccdPartyStatement) {
        if (Stream.of(ccdPartyStatement.getOfferContent(), ccdPartyStatement.getOfferCompletionDate(),
            ccdPartyStatement.getPaymentIntention()).anyMatch(Objects::nonNull)) {
            return Offer.builder()
                .content(ccdPartyStatement.getOfferContent())
                .completionDate(ccdPartyStatement.getOfferCompletionDate())
                .paymentIntention(paymentIntentionMapper.from(ccdPartyStatement.getPaymentIntention()))
                .build();
        }

        return null;
    }
}
