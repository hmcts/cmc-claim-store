package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.ClaimantLinkException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ClaimantResponseAlreadySubmittedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_CLAIMANT_RESPONSE;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_PAYMENT_INTENTION;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_RESPONSE;
import static uk.gov.hmcts.cmc.domain.utils.PartyUtils.isCompanyOrOrganisation;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.isResponseStatesPaid;

@Service
public class ClaimantResponseRule {

    public void isValid(Claim claim) {
        if (!isDefendantCompanyOrOrganisation(claim)) {
            ClaimantResponse claimantResponse = claim.getClaimantResponse()
                .orElseThrow(() -> new IllegalStateException(MISSING_CLAIMANT_RESPONSE));
            if (claimantResponse.getType() == ClaimantResponseType.ACCEPTATION
                && isFormaliseOptionExpectedForResponse(claim.getResponse()
                        .orElseThrow(() -> new IllegalStateException(MISSING_RESPONSE)))
            ) {
                ResponseAcceptation responseAcceptation = (ResponseAcceptation) claimantResponse;
                if (!responseAcceptation.getFormaliseOption().isPresent()) {
                    throw new BadRequestException("Formalise option can not be null");
                }
                checkCourtDeterminationAndPaymentIntention(responseAcceptation);
            }
        }
    }

    public void assertCanBeRequested(Claim claim, String claimantId) {
        if (!isClaimLinkedWithClaimant(claim, claimantId)) {
            throw new ClaimantLinkException(
                String.format("Claim %s is not linked with claimant %s", claim.getReferenceNumber(), claimantId)
            );
        }

        if (isDefendantResponseNotSubmitted(claim)) {
            throw new ForbiddenActionException(
                String.format(
                    "Defendant response for the claim %s has not been submitted, but claimant response requested",
                    claim.getExternalId()
                )
            );
        }

        if (isClaimantResponseAlreadySubmitted(claim)) {
            throw new ClaimantResponseAlreadySubmittedException(claim.getExternalId());
        }
    }

    private void checkCourtDeterminationAndPaymentIntention(ResponseAcceptation responseAcceptation) {
        Optional<CourtDetermination> courtDetermination = responseAcceptation.getCourtDetermination();
        Optional<PaymentIntention> claimantPaymentIntention = responseAcceptation.getClaimantPaymentIntention();

        boolean isBothEmpty = !claimantPaymentIntention.isPresent() && !courtDetermination.isPresent();
        boolean isBothPresent = (claimantPaymentIntention.isPresent() || courtDetermination.isPresent())
            && (claimantPaymentIntention.isPresent() && courtDetermination.isPresent());
        if (!isBothEmpty && !isBothPresent) {
            throw new BadRequestException(
                "Court determination should be present when "
                + "claimant payment intention is present or vice versa"
            );
        }
    }

    private boolean isDefendantResponseNotSubmitted(Claim claim) {
        return claim.getRespondedAt() == null;
    }

    private boolean isClaimLinkedWithClaimant(Claim claim, String claimantId) {
        return claim.getSubmitterId() != null && claim.getSubmitterId().equals(claimantId);
    }

    private boolean isClaimantResponseAlreadySubmitted(Claim claim) {
        return claim.getClaimantRespondedAt().isPresent();
    }

    private boolean isDefendantCompanyOrOrganisation(Claim claim) {
        Response response = claim.getResponse()
            .orElseThrow(() -> new IllegalArgumentException(MISSING_RESPONSE));
        return isCompanyOrOrganisation(response.getDefendant());
    }

    public static boolean isFormaliseOptionExpectedForResponse(Response response) {
        switch (response.getResponseType()) {
            case PART_ADMISSION:
                if (isResponseStatesPaid(response)) {
                    return false;
                }

                return ((PartAdmissionResponse) response).getPaymentIntention()
                    .orElseThrow(() -> new IllegalStateException(MISSING_PAYMENT_INTENTION))
                    .getPaymentOption() != PaymentOption.IMMEDIATELY;
            case FULL_ADMISSION:
                return ((FullAdmissionResponse) response).getPaymentIntention()
                    .getPaymentOption() != PaymentOption.IMMEDIATELY;
            case FULL_DEFENCE:
                return false;
            default:
                return true;
        }
    }
}
