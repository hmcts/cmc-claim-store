package uk.gov.hmcts.cmc.domain.models;

import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.party.Party;

public class Mediation {
    private final int siteId;
    private final String caseNumber;
    private final int caseType;
    private final Amount amount;
    private final Party partyType;
    private final String contactName;
    private final String contactDetail;
    private final int contactNumber;
    private final int checkList;
    private final int partyStatus;

    public Mediation(
        String caseNumber,
        Amount amount,
        Party partyType,
        String contactName,
        String contactDetail,
        int contactNumber
    ) {
        this.siteId = 4;
        this.caseNumber = caseNumber;
        this.caseType = 1;
        this.amount = amount; //claim amount not including claim fee
        this.partyType = partyType; // 1 for claimant and 2 for defendant
        this.contactName = contactName; //mediation contact person
        this.contactDetail = contactDetail; //mediation email address
        this.contactNumber = contactNumber; //mediation phone number
        this.checkList = 4;
        this.partyStatus = 5;
    }

}
