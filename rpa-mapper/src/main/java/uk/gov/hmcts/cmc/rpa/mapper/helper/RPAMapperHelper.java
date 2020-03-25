package uk.gov.hmcts.cmc.rpa.mapper.helper;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.utils.ResponseUtils;
import uk.gov.hmcts.cmc.rpa.DateFormatter;
import uk.gov.hmcts.cmc.rpa.mapper.json.NullAwareJsonObjectBuilder;

import java.time.LocalDate;
import java.util.Optional;
import javax.json.JsonObject;

public class RPAMapperHelper {

    private static final String MISSING_MONEY_RECEIVED_DATE = "Missing money received date";

    private RPAMapperHelper() {
        // NO-OP
    }

    public static String prependWithTradingAs(String value) {
        return "T/A " + value;
    }

    public static boolean isAddressAmended(Party ownParty, TheirDetails oppositeParty) {
        return !ownParty.getAddress().equals(oppositeParty.getAddress());
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

    private static boolean isResponseStatesPaidAccepted(Claim claim) {
        Optional<Response> response = claim.getResponse();
        Optional<ClaimantResponse> claimantResponse = claim.getClaimantResponse();

        if (response.filter(ResponseUtils::isResponseStatesPaid).isPresent() && claimantResponse.isPresent()) {
                return claimantResponse.get().getType() == ClaimantResponseType.ACCEPTATION;
        }
        return false;
    }

    private static LocalDate statesPaidPaymentDeclarationDate(Response response) {
        switch (response.getResponseType()) {
            case FULL_DEFENCE:
                FullDefenceResponse fullDefenceResponse = (FullDefenceResponse) response;
                return fullDefenceResponse.getPaymentDeclaration()
                    .orElseThrow((() -> new IllegalStateException(MISSING_MONEY_RECEIVED_DATE)))
                    .getPaidDate();

            case PART_ADMISSION:
                PartAdmissionResponse partAdmissionResponse = (PartAdmissionResponse) response;
                return partAdmissionResponse.getPaymentDeclaration()
                    .orElseThrow((() -> new IllegalStateException(MISSING_MONEY_RECEIVED_DATE)))
                    .getPaidDate();

            default:
                throw new IllegalStateException("Invalid response type " + response.getResponseType());
        }
    }
}
