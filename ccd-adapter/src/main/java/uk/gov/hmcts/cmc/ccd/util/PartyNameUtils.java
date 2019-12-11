package uk.gov.hmcts.cmc.ccd.util;

import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.NamedParty;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

public class PartyNameUtils {

    private PartyNameUtils() {
        // Utility class, no instances
    }

    public static String getPartyNameFor(NamedParty party) {
        StringBuilder name = new StringBuilder(party.getName());

        if (party instanceof SoleTrader) {
            (((SoleTrader) party).getBusinessName()).ifPresent(t -> name.append(" T/A ").append(t));
        }

        return name.toString();
    }

    public static String getTheirDetailsNameFor(TheirDetails theirDetail) {
        StringBuilder name = new StringBuilder(theirDetail.getName());

        if (theirDetail instanceof SoleTraderDetails) {
            (((SoleTraderDetails) theirDetail).getBusinessName()).ifPresent(t -> name.append(" T/A ").append(t));
        }

        return name.toString();
    }
}
