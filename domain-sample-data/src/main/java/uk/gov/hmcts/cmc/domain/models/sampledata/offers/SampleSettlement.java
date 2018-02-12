package uk.gov.hmcts.cmc.domain.models.sampledata.offers;

import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SampleSettlement {

    public static final PartyStatement offerPartyStatement
        = SamplePartyStatement.validDefaults();

    public static final PartyStatement rejectPartyStatement
        = SamplePartyStatement.builder().withStatementType(StatementType.REJECTION).build();

    public static final PartyStatement acceptPartyStatement
        = SamplePartyStatement.builder().withStatementType(StatementType.ACCEPTATION).build();

    private List<PartyStatement> partyStatements = new ArrayList<>(Collections.singletonList(offerPartyStatement));

    public static Settlement validDefaults() {

        return builder().build();
    }

    public static SampleSettlement builder() {
        return new SampleSettlement();
    }

    public Settlement build() {
        Settlement settlement = new Settlement();

        partyStatements.stream()
            .filter(p -> p.getType().equals(StatementType.OFFER))
            .forEach(p -> settlement.makeOffer(p.getOffer().orElse(null), p.getMadeBy()));

        partyStatements.stream()
            .filter(p -> p.getType().equals(StatementType.REJECTION))
            .forEach(p -> settlement.reject(p.getMadeBy()));

        partyStatements.stream()
            .filter(p -> p.getType().equals(StatementType.ACCEPTATION))
            .forEach(p -> settlement.accept(p.getMadeBy()));

        return settlement;
    }

    public SampleSettlement withPartyStatement(PartyStatement partyStatement) {
        this.partyStatements = new ArrayList<>(partyStatements);

        return this;
    }
}
