package uk.gov.hmcts.cmc.ccd.mapper.offers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
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
            .map(partyStatementMapper::to)
            .collect(Collectors.toList());

    }

    public Settlement fromCCDDefendant(CCDDefendant ccdDefendant) {
        if (CollectionUtils.isEmpty(ccdDefendant.getSettlementPartyStatements())) {
            return null;
        }


        Settlement settlement = new Settlement();
        ccdDefendant.getSettlementPartyStatements().stream()
            .map(partyStatementMapper::from)
            .forEach(partyStatement -> addPartyStatement(partyStatement, settlement));

        return settlement;
    }

    private void addPartyStatement(PartyStatement partyStatement, Settlement settlement) {
        if (StatementType.OFFER.equals(partyStatement.getType())) {
            settlement.addOffer(
                partyStatement.getOffer().orElse(null),
                partyStatement.getMadeBy(),
                partyStatement.getId()
            );
        }

        if (StatementType.REJECTION.equals(partyStatement.getType())) {
            settlement.reject(partyStatement.getMadeBy(), partyStatement.getId());
        }

        if (StatementType.ACCEPTATION.equals(partyStatement.getType())) {
            settlement.accept(partyStatement.getMadeBy(), partyStatement.getId());
        }

        if (StatementType.COUNTERSIGNATURE.equals(partyStatement.getType())) {
            settlement.countersign(partyStatement.getMadeBy(), partyStatement.getId());
        }
    }
}
