package uk.gov.hmcts.cmc.domain.models.sampledata.offers;

import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SampleSettlement {

    public static final PartyStatement offerPartyStatement
        = SamplePartyStatement.validDefaults();

    public static final PartyStatement rejectPartyStatement = SamplePartyStatement.builder()
        .withStatementType(StatementType.REJECTION)
        .withMadeBy(MadeBy.CLAIMANT)
        .withOffer(null)
        .build();

    public static final PartyStatement acceptPartyStatement = SamplePartyStatement.builder()
        .withStatementType(StatementType.ACCEPTATION)
        .withMadeBy(MadeBy.CLAIMANT)
        .withOffer(null)
        .build();

    public static final PartyStatement counterSignPartyStatement = SamplePartyStatement.builder()
        .withStatementType(StatementType.COUNTERSIGNATURE)
        .withMadeBy(MadeBy.DEFENDANT)
        .withOffer(null)
        .build();

    private List<PartyStatement> partyStatements = new ArrayList<>(Collections.singletonList(offerPartyStatement));

    public static Settlement validDefaults() {

        return builder().build();
    }

    public static SampleSettlement builder() {
        return new SampleSettlement();
    }

    public Settlement build() {
        Settlement settlement = new Settlement();
        partyStatements.forEach(partyStatement -> addPartyStatement(partyStatement, settlement));
        return settlement;
    }

    public SampleSettlement withPartyStatements(PartyStatement... partyStatement) {
        List<PartyStatement> statements = Arrays.asList(partyStatement);
        this.partyStatements = new ArrayList<>(statements);

        return this;
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
