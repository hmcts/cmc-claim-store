package uk.gov.hmcts.cmc.rpa.mapper.helper;

import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import javax.json.JsonObject;

public class RPAMapperHelper {

    private RPAMapperHelper() {
        // NO-OP
    }

    public static String prependWithTradingAs(String value) {
        return "T/A " + value;
    }

    public static boolean isAddressAmended(Party ownParty, TheirDetails oppositeParty) {
        return !ownParty.getAddress().equals(oppositeParty.getAddress());
    }

    public static JsonObject toJson(RepaymentPlan repaymentPlan) {
        if (repaymentPlan != null) {
            return new NullAwareJsonObjectBuilder()
                .add("amount", repaymentPlan.getInstalmentAmount())
                .add("firstPayment", DateFormatter.format(repaymentPlan.getFirstPaymentDate()))
                .add("frequency", repaymentPlan.getPaymentSchedule().name())
                .build();
        }
        return null;
    }
}
