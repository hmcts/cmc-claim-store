package uk.gov.hmcts.cmc.domain.utils;

import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;

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
}
