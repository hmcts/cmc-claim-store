package uk.gov.hmcts.cmc.domain.utils;

import uk.gov.hmcts.cmc.domain.models.response.*;

public class ResponseUtils {

    private ResponseUtils() {
        // utility class, no instances
    }

    public static boolean isResponseStatesPaid(Response response) {
        if (response == null) {
            return false;
        }

        switch (response.getResponseType()) {
            case FULL_DEFENCE:
                return ((FullDefenceResponse) response).getDefenceType() == DefenceType.ALREADY_PAID;
            case PART_ADMISSION:
                return ((PartAdmissionResponse) response).getPaymentDeclaration().isPresent();
            default:
                return false;
        }
    }

    public static boolean isResponseFullDefenceStatesPaid(Response response) {
        if (response == null) {
            return false;
        }

        if (response.getResponseType() == ResponseType.FULL_DEFENCE) {
            return isResponseStatesPaid(response);
        }

        return false;
    }
}
