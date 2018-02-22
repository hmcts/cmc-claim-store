package uk.gov.hmcts.cmc.ccd.mapper.offers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDPartyStatement;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDSettlement;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SettlementMapper implements Mapper<CCDSettlement, Settlement> {

    private PartyStatementMapper partyStatementMapper;

    @Autowired
    public SettlementMapper(PartyStatementMapper partyStatementMapper) {
        this.partyStatementMapper = partyStatementMapper;
    }

    @Override
    public CCDSettlement to(Settlement settlement) {
        CCDSettlement.CCDSettlementBuilder builder = CCDSettlement.builder();

        List<CCDCollectionElement<CCDPartyStatement>> partyStatements = settlement.getPartyStatements().stream()
            .map(partyStatement -> partyStatementMapper.to(partyStatement))
            .map(partyStatement -> CCDCollectionElement.<CCDPartyStatement>builder().value(partyStatement).build())
            .collect(Collectors.toList());

        builder.partyStatements(partyStatements);

        return builder.build();
    }

    @Override
    public Settlement from(CCDSettlement ccdSettlement) {
        List<PartyStatement> partyStatements = ccdSettlement.getPartyStatements().stream()
            .map(CCDCollectionElement::getValue)
            .map(partyStatement -> partyStatementMapper.from(partyStatement))
            .collect(Collectors.toList());

        Settlement settlement = new Settlement();
        partyStatements.forEach(partyStatement -> addPartyStatement(partyStatement, settlement));

        return settlement;
    }

    private void addPartyStatement(PartyStatement partyStatement, Settlement settlement) {
        if (partyStatement.getType().equals(StatementType.OFFER)) {
            settlement.makeOffer(partyStatement.getOffer().orElse(null), partyStatement.getMadeBy());
        }

        if (partyStatement.getType().equals(StatementType.REJECTION)) {
            settlement.reject(partyStatement.getMadeBy());
        }

        if (partyStatement.getType().equals(StatementType.ACCEPTATION)) {
            settlement.accept(partyStatement.getMadeBy());
        }

        if (partyStatement.getType().equals(StatementType.COUNTERSIGNATURE)) {
            settlement.countersign(partyStatement.getMadeBy());
        }
    }
}
