package uk.gov.hmcts.cmc.claimstore.utils;

import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;

import java.util.function.Predicate;

import static uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType.ACCEPTATION;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType.REJECTION;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

public class ClaimantResponseHelper {

    private ClaimantResponseHelper() {
        // Utility class, no instances
    }

    public static boolean isSettlePreJudgment(ClaimantResponse response) {
        if (response.getType().equals(ACCEPTATION)) {
            ResponseAcceptation responseAcceptation = (ResponseAcceptation) response;
            return responseAcceptation.getSettleForAmount()
                .filter(Predicate.isEqual(YES))
                .isPresent();
        }
        return false;
    }

    public static boolean isReferredToJudge(ClaimantResponse response) {
        if (response.getType().equals(ACCEPTATION)) {
            ResponseAcceptation responseAcceptation = (ResponseAcceptation) response;
            return responseAcceptation.getFormaliseOption()
                .filter(Predicate.isEqual(FormaliseOption.REFER_TO_JUDGE))
                .isPresent();
        }
        return false;
    }

    public static boolean isOptedForMediation(ClaimantResponse claimantResponse) {
        return claimantResponse.getType() == REJECTION
            && ((ResponseRejection) claimantResponse).getFreeMediation()
            .filter(Predicate.isEqual(YES))
            .isPresent();
    }

    public static boolean isIntentToProceed(ClaimantResponse claimantResponse) {
        return claimantResponse.getType() == REJECTION
            && ((ResponseRejection) claimantResponse).getDirectionsQuestionnaire().isPresent();
    }
}
