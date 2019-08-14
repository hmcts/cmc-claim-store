package uk.gov.hmcts.cmc.claimstore.utils;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;

import java.util.function.Predicate;

import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

public class ResponseHelper {

    private ResponseHelper() {
        // Utility class, no instances
    }

    public static boolean admissionResponse(Response response) {
        if (response != null && (response.getResponseType().equals(ResponseType.PART_ADMISSION)
            || response.getResponseType().equals(ResponseType.FULL_ADMISSION))) {
            return true;
        }
        return false;
    }

    public static String getResponseType(Claim claim) {
        Response response = claim.getResponse().orElse(null);
        return response != null ? response.getResponseType().name() : null;
    }

    public static boolean isOptedForMediation(Response response) {
        return response.getFreeMediation()
            .filter(Predicate.isEqual(YES))
            .isPresent();
    }
}
