package uk.gov.hmcts.cmc.ccd.mapper.offers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDMadeBy;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDPartyStatement;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDStatementType;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;

@Component
public class PartyStatementMapper implements Mapper<CCDPartyStatement, PartyStatement> {

    private final OfferMapper offerMapper;

    public PartyStatementMapper(OfferMapper offerMapper) {
        this.offerMapper = offerMapper;
    }

    @Override
    public CCDPartyStatement to(PartyStatement partyStatement) {
        CCDPartyStatement.CCDPartyStatementBuilder builder = CCDPartyStatement.builder();
        builder.type(CCDStatementType.valueOf(partyStatement.getType().name()));
        builder.madeBy(CCDMadeBy.valueOf(partyStatement.getMadeBy().name()));
        partyStatement.getOffer().ifPresent(offer -> builder.offer(offerMapper.to(offer)));
        return builder.build();
    }

    @Override
    public PartyStatement from(CCDPartyStatement partyStatement) {
        StatementType statementType = StatementType.valueOf(partyStatement.getType().name());
        MadeBy madeBy = MadeBy.valueOf(partyStatement.getMadeBy().name());
        return new PartyStatement(statementType, madeBy, offerMapper.from(partyStatement.getOffer()));
    }
}
