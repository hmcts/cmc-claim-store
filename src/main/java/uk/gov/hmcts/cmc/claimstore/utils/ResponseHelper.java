package uk.gov.hmcts.cmc.claimstore.utils;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;

public class ResponseHelper {

    private ResponseHelper() {
        // Utility class, no instances
    }

    public static boolean admissionResponse(Claim claim) {
        if (claim.getResponse().isPresent()) {
            Response response = claim.getResponse().get();
            return response.getResponseType().equals(ResponseType.PART_ADMISSION)
                || response.getResponseType().equals(ResponseType.FULL_ADMISSION);
        } else {
            return false;
        }
    }
}
