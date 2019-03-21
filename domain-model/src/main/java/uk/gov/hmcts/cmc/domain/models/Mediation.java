package uk.gov.hmcts.cmc.domain.models;

import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.math.BigDecimal;
import java.util.Optional;

public class Mediation {
    private final int siteId;
    private final String caseNumber;
    private final int caseType;
    private final Optional<BigDecimal> amount;
    private final int partyType;
    private final Optional<String> contactName;
    private final Optional<String> contactNumber;
    private final int checkList;
    private final int partyStatus;

    public Mediation(
        String caseNumber,
        Optional<BigDecimal> amount,
        int partyType,
        Optional<String> contactName,
        Optional<String> contactNumber
    ) {
        this.siteId = 4;
        this.caseNumber = caseNumber;
        this.caseType = 1;
        this.amount = amount; //claim amount not including claim fee
        this.partyType = partyType; // 1 for claimant and 2 for defendant
        this.contactName = contactName; //mediation contact person
        this.contactNumber = contactNumber; //mediation phone number
        this.checkList = 4;
        this.partyStatus = 5;
    }

}
