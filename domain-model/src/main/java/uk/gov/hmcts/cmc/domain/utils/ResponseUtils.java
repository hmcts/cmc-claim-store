package uk.gov.hmcts.cmc.domain.utils;

import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.function.Predicate;

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

    public static boolean isResponsePartAdmitPayImmediately(Response response) {
        if (response.getResponseType() != ResponseType.PART_ADMISSION) {
            return false;
        }

        PartAdmissionResponse partAdmissionResponse = (PartAdmissionResponse) response;
        return partAdmissionResponse.getPaymentIntention()
            .map(PaymentIntention::getPaymentOption)
            .filter(Predicate.isEqual(PaymentOption.IMMEDIATELY))
            .isPresent();
    }

    public static boolean isAdmissionResponse(Response response) {
        ResponseType responseType = response.getResponseType();
        return responseType == ResponseType.FULL_ADMISSION || responseType == ResponseType.PART_ADMISSION;
    }

    public static boolean isPartAdmission(Response response) {
        ResponseType responseType = response.getResponseType();
        return responseType == ResponseType.PART_ADMISSION;
    }

    public static boolean isFullDefenceDisputeAndNoMediation(Response response) {
        return isFullDefenceAndNoMediation(response) && ((FullDefenceResponse) response)
            .getDefenceType().equals(DefenceType.DISPUTE);
    }

    public static boolean isFullDefenceAndNoMediation(Response response) {
        return isFullDefence(response) && isNoMediation(response);
    }

    public static boolean isFullDefenceDispute(Response response) {
        return isFullDefence(response) && ((FullDefenceResponse) response)
            .getDefenceType().equals(DefenceType.DISPUTE);
    }

    public static boolean isFullDefence(Response response) {
        return response.getResponseType().equals(ResponseType.FULL_DEFENCE);
    }

    public static boolean isNoMediation(Response response) {
        return response.getFreeMediation().filter(Predicate.isEqual(YesNoOption.NO)).isPresent();
    }
}

