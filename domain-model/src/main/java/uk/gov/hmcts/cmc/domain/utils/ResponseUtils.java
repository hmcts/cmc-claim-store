package uk.gov.hmcts.cmc.domain.utils;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Predicate;

import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

public class ResponseUtils {

    private static final String MISSING_PAYMENT_DECLARATION_DATE = "Missing payment declaration date";

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

    public static boolean isResponseStatesPaidAccepted(Claim claim) {
        Optional<Response> response = claim.getResponse();
        Optional<ClaimantResponse> claimantResponse = claim.getClaimantResponse();

        if (response.filter(ResponseUtils::isResponseStatesPaid).isPresent() && claimantResponse.isPresent()) {
            return claimantResponse.get().getType() == ClaimantResponseType.ACCEPTATION;
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
        return isFullDefence(response) && !hasDefendantOptedForMediation(response);
    }

    public static boolean isFullDefenceDispute(Response response) {
        return isFullDefence(response) && ((FullDefenceResponse) response)
            .getDefenceType().equals(DefenceType.DISPUTE);
    }

    public static boolean isFullDefence(Response response) {
        return response.getResponseType().equals(ResponseType.FULL_DEFENCE);
    }

    public static boolean hasDefendantOptedForMediation(Response response) {
        return response.getFreeMediation().filter(Predicate.isEqual(YES)).isPresent();
    }

    public static LocalDate statesPaidPaymentDeclarationDate(Response response) {
        switch (response.getResponseType()) {
            case FULL_DEFENCE:
                FullDefenceResponse fullDefenceResponse = (FullDefenceResponse) response;
                return fullDefenceResponse.getPaymentDeclaration()
                    .orElseThrow((() -> new IllegalStateException(MISSING_PAYMENT_DECLARATION_DATE)))
                    .getPaidDate();

            case PART_ADMISSION:
                PartAdmissionResponse partAdmissionResponse = (PartAdmissionResponse) response;
                return partAdmissionResponse.getPaymentDeclaration()
                    .orElseThrow((() -> new IllegalStateException(MISSING_PAYMENT_DECLARATION_DATE)))
                    .getPaidDate();

            default:
                throw new IllegalStateException("Invalid response type " + response.getResponseType());
        }
    }
}

