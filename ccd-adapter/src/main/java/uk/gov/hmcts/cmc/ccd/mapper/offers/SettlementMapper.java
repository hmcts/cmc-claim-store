package uk.gov.hmcts.cmc.ccd.mapper.offers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDPartyStatementArrayElement;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDSettlement;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SettlementMapper implements Mapper<CCDSettlement, Settlement> {

    private PartStatementMapper partyStatementMapper;

    @Autowired
    public SettlementMapper(PartStatementMapper partyStatementMapper) {
        this.partyStatementMapper = partyStatementMapper;
    }

    @Override
    public CCDSettlement to(Settlement settlement) {
        CCDSettlement.CCDSettlementBuilder builder = CCDSettlement.builder();

        List<CCDPartyStatementArrayElement> partyStatements = settlement.getPartyStatements().stream()
            .map(partyStatement -> partyStatementMapper.to(partyStatement))
            .map(ccdPartyStatement -> CCDPartyStatementArrayElement.builder().value(ccdPartyStatement).build())
            .collect(Collectors.toList());

        builder.partyStatements(partyStatements);

        return builder.build();
    }

    @Override
    public Settlement from(CCDSettlement settlement) {
        List<PartyStatement> partyStatements = settlement.getPartyStatements().stream()
            .map(CCDPartyStatementArrayElement::getValue)
            .map(partyStatement -> partyStatementMapper.from(partyStatement))
            .collect(Collectors.toList());

        Settlement output = new Settlement();

        partyStatements.stream()
            .filter(p -> p.getType().equals(StatementType.OFFER))
            .forEach(p -> output.makeOffer(p.getOffer().orElse(null), p.getMadeBy()));

        partyStatements.stream()
            .filter(p -> p.getType().equals(StatementType.REJECTION))
            .forEach(p -> output.reject(p.getMadeBy()));

        partyStatements.stream()
            .filter(p -> p.getType().equals(StatementType.ACCEPTATION))
            .forEach(p -> output.accept(p.getMadeBy()));

        return output;
    }
}
