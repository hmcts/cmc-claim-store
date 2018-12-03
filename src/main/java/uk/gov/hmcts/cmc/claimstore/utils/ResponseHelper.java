package uk.gov.hmcts.cmc.claimstore.utils;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;

public class ResponseHelper {

    private ResponseHelper() {
        // Utility class, no instances
    }

    public static boolean admissionResponse(Claim claim) {
        Response response = claim.getResponse().orElse(null);
        if (response != null && (response.getResponseType().equals(ResponseType.PART_ADMISSION)
            || response.getResponseType().equals(ResponseType.FULL_ADMISSION))) {
            return true;
        }
        return false;
    }

    public static String getResponseType(Claim claim) {
        Response response =  claim.getResponse().orElse(null);
        return response != null ? response.getResponseType().name() : null;
    }
}
