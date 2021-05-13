package uk.gov.hmcts.cmc.rpa.mapper.helper;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import java.time.LocalDate;
import javax.json.JsonObject;

import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.isResponseStatesPaidAccepted;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.statesPaidPaymentDeclarationDate;

public class RPAMapperHelper {

    private static final String MISSING_MONEY_RECEIVED_DATE = "Missing money received date";

    private RPAMapperHelper() {
        // NO-OP
    }

    public static String prependWithTradingAs(String value) {
        return "T/A " + value;
    }

    public static boolean isAddressAmended(Party ownParty, TheirDetails oppositeParty) {
        return !ownParty.getAddress().equals(oppositeParty.getclaimantProvidedAddress());
    }

    public static LocalDate claimantPaidOnDate(Claim claim) {
        if (isResponseStatesPaidAccepted(claim)) {
            return statesPaidPaymentDeclarationDate(claim.getResponse()
                .orElseThrow(() -> new IllegalStateException(MISSING_MONEY_RECEIVED_DATE))
            );
        }
        return claim.getMoneyReceivedOn()
            .orElseThrow(() -> new IllegalStateException(MISSING_MONEY_RECEIVED_DATE));
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
