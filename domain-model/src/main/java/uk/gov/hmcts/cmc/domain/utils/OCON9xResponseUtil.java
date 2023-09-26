package uk.gov.hmcts.cmc.domain.utils;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.function.Predicate;

public class OCON9xResponseUtil {

    private OCON9xResponseUtil(){
        // NO-OP
    }

    public static boolean defendantFullDefenceMediationOCON9x(Claim claim){

        if (isFullDefenceResponse(claim)) {
            System.out.println("****************: isFullDefenceResponse(claim) true ************************");
        } else {
            System.out.println("****************: isFullDefenceResponse(claim) false ************************");
        }


        if (isDefendantMediation(claim)) {
            System.out.println("****************: isDefendantMediation(claim) true ************************");
        } else {
            System.out.println("****************: isDefendantMediation(claim) false ************************");
        }

        if (hasClaimantRejected(claim)) {
            System.out.println("****************: hasClaimantRejected(claim) true ************************");
        } else {
            System.out.println("****************: hasClaimantRejected(claim) false ************************");
        }

        if (hasClaimantRejectedMediation(claim)) {
            System.out.println("****************: hasClaimantRejectedMediation(claim) true ************************");
        } else {
            System.out.println("****************: hasClaimantRejectedMediation(claim) false ************************");
        }

        if (isPaperDefenceFormIssued(claim)) {
            System.out.println("****************: isPaperDefenceFormIssued(claim) true ************************");
        } else {
            System.out.println("****************: isPaperDefenceFormIssued(claim) false ************************");
        }

        return isFullDefenceResponse(claim) &&
                isDefendantMediation(claim) &&
                hasClaimantRejected(claim) &&
                hasClaimantRejectedMediation(claim) &&
                isPaperDefenceFormIssued(claim);
    }

    private static boolean isFullDefenceResponse(Claim claim) {
        return claim.getResponse()
            .map(Response::getResponseType)
            .filter(ResponseType.FULL_DEFENCE::equals)
            .isPresent() ;
    }

    private static boolean isDefendantMediation(Claim claim) {
        return claim.getResponse()
            .flatMap(Response::getFreeMediation)
            .filter(YesNoOption.YES::equals)
            .isPresent();
    }

    private static boolean hasClaimantRejected(Claim claim) {
        return claim.getClaimantResponse()
            .map(ClaimantResponse::getType)
            .filter(ClaimantResponseType.REJECTION::equals)
            .isPresent();
    }

    private static boolean hasClaimantRejectedMediation(Claim claim) {
        return claim.getClaimantResponse()
            .flatMap(ClaimantResponse::getFreeMediation)
            .filter(YesNoOption.NO::equals)
            .isPresent();
    }

    private static boolean isPaperDefenceFormIssued(Claim claim) {
        System.out.println("***************** Paper form issue date"+claim.getPaperFormIssueDate()+ "***************************");
        return claim.getPaperFormIssueDate() != null;
    }
}

