package uk.gov.hmcts.cmc.ccd.mapper.offers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDPartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SettlementMapper {

    private PartyStatementMapper partyStatementMapper;

    @Autowired
    public SettlementMapper(PartyStatementMapper partyStatementMapper) {
        this.partyStatementMapper = partyStatementMapper;
    }

    public List<CCDCollectionElement<CCDPartyStatement>> toCCDPartyStatements(Settlement settlement) {
        if (settlement == null || settlement.getPartyStatements() == null
            || settlement.getPartyStatements().isEmpty()) {
            return null;
        }
        return settlement.getPartyStatements().stream()
            .map(partyStatement -> partyStatementMapper.to(partyStatement))
            .map(partyStatement -> CCDCollectionElement.<CCDPartyStatement>builder().value(partyStatement).build())
            .collect(Collectors.toList());

    }

    public Settlement fromCCDDefendant(CCDDefendant ccdDefendant) {
        if (ccdDefendant.getSettlementPartyStatements() == null
            || ccdDefendant.getSettlementPartyStatements().isEmpty()) {
            return null;
        }
        List<PartyStatement> partyStatements = ccdDefendant.getSettlementPartyStatements().stream()
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
