package uk.gov.hmcts.cmc.domain.utils;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;

public class ResponseUtils {

    private ResponseUtils() {
        // utility class, no instances
    }

    public static boolean isResponseFullDefenceStatesPaid(Claim claim) {
        ResponseType responseType = claim.getResponse().orElseThrow(IllegalArgumentException::new).getResponseType();

        if (responseType == ResponseType.FULL_DEFENCE) {
            FullDefenceResponse fullDefenceResponse = (FullDefenceResponse) claim.getResponse()
                .orElseThrow(IllegalArgumentException::new);
            return fullDefenceResponse.getDefenceType() == DefenceType.ALREADY_PAID;
        }
        return false;
    }
}
