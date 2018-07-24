package uk.gov.hmcts.cmc.rpa.mapper.helper;

import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Party;

public class RPAMapperHelper {

    private RPAMapperHelper() {
        // NO-OP
    }

    public static String prependWithTradingAs(String value) {
        return "T/A " + value;
    }

    public static boolean isAddressAmended(Party ownParty, TheirDetails oppositeParty) {
        return !ownParty.getAddress().equals(oppositeParty.getAddress()) ? true : false;
    }
}
