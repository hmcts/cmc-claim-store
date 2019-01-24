package uk.gov.hmcts.cmc.ccd.mapper.offers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDPartyStatement;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDMadeBy;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDStatementType;
import uk.gov.hmcts.cmc.ccd.mapper.PaymentIntentionMapper;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class PartyStatementMapper {

    private final PaymentIntentionMapper paymentIntentionMapper;

    @Autowired
    public PartyStatementMapper(PaymentIntentionMapper paymentIntentionMapper) {
        this.paymentIntentionMapper = paymentIntentionMapper;
    }

    public CCDCollectionElement<CCDPartyStatement> to(PartyStatement partyStatement) {
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

        return CCDCollectionElement.<CCDPartyStatement>builder()
            .value(builder.build())
            .id(partyStatement.getId())
            .build();
    }

    public PartyStatement from(CCDCollectionElement<CCDPartyStatement> ccdPartyStatement) {
        CCDPartyStatement value = ccdPartyStatement.getValue();
        PartyStatement.PartyStatementBuilder partyStatementBuilder = PartyStatement.builder();

        Optional.ofNullable(value.getMadeBy()).ifPresent(ccdMadeBy ->
            partyStatementBuilder.madeBy(MadeBy.valueOf(ccdMadeBy.name()))
        );

        Optional.ofNullable(value.getType()).ifPresent(ccdStatementType ->
            partyStatementBuilder.type(StatementType.valueOf(ccdStatementType.name()))
        );

        partyStatementBuilder
            .offer(buildOfferFromCCDPartyStatement(value))
            .id(ccdPartyStatement.getId());

        return partyStatementBuilder.build();

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
