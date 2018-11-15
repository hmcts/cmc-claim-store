package uk.gov.hmcts.cmc.domain.utils;

import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;

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
            case FULL_ADMISSION:
            default:
                return false;
        }
    }
}
