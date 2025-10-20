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

    private static final Predicate<Claim> FULL_DEFENCE_RESPONSE = claim -> claim.getResponse()
        .map(Response::getResponseType)
        .filter(ResponseType.FULL_DEFENCE::equals)
        .isPresent();

    private static final Predicate<Claim> DEFENDANT_MEDIATION = claim -> claim.getResponse()
        .flatMap(Response::getFreeMediation)
        .filter(YesNoOption.YES::equals)
        .isPresent();

    private static final Predicate<Claim> CLAIMANT_REJECTION = claim -> claim.getClaimantResponse()
        .map(ClaimantResponse::getType)
        .filter(ClaimantResponseType.REJECTION::equals)
        .isPresent();

    private static final Predicate<Claim> CLAIMANT_MEDIATION_REJECTION = claim -> claim.getClaimantResponse()
        .flatMap(ClaimantResponse::getFreeMediation)
        .filter(YesNoOption.NO::equals)
        .isPresent();

    private static final Predicate<Claim> IS_PAPER_DEFENCE_FORM_ISSUED = claim -> claim.getPaperFormIssueDate() != null;

    public static boolean defendantFullDefenceMediationOCON9x(Claim claim) {

        return
            FULL_DEFENCE_RESPONSE.test(claim)
                && DEFENDANT_MEDIATION.test(claim)
                && CLAIMANT_REJECTION.test(claim)
                && CLAIMANT_MEDIATION_REJECTION.test(claim)
                && IS_PAPER_DEFENCE_FORM_ISSUED.test(claim);
    }
}

