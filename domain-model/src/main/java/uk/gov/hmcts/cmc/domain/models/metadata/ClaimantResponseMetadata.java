package uk.gov.hmcts.cmc.domain.models.metadata;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.function.Predicate.isEqual;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class ClaimantResponseMetadata {
    private final LocalDateTime claimantRespondedAt;
    private final ClaimantResponseType claimantResponseType;
    private final Boolean fullPaymentReceived;
    private final Boolean settleForAmount;
    private final PaymentPlanMetadata claimantPaymentPlan;
    private final PaymentPlanMetadata courtPaymentPlan;
    private final DecisionType courtDecision;
    private final FormaliseOption formaliseOption;
    private final Boolean mediation;

    static ClaimantResponseMetadata fromClaim(Claim claim) {
        final Optional<ClaimantResponse> optionalClaimantResponse = claim.getClaimantResponse();
        if (!optionalClaimantResponse.isPresent()) {
            return null;
        }

        final ClaimantResponse claimantResponse = optionalClaimantResponse.get();
        final LocalDateTime claimantRespondedAt = claim.getClaimantRespondedAt().orElse(null);
        final Optional<YesNoOption> optionalSettleForAmount = claimantResponse.getSettleForAmount();
        final Boolean settleForAmount = optionalSettleForAmount.isPresent()
            ? optionalSettleForAmount.filter(isEqual(YesNoOption.YES)).isPresent()
            : null;
        final Optional<BigDecimal> optionalAmountPaid = claimantResponse.getAmountPaid();
        final Boolean fullPaymentReceived = optionalAmountPaid.isPresent()
            ? optionalAmountPaid.filter(isEqual(claim.getTotalAmountTillToday())).isPresent()
            : null;
        final ClaimantResponseType claimantResponseType = claimantResponse.getType();

        if (claimantResponse instanceof ResponseAcceptation) {
            final ResponseAcceptation responseAcceptation = (ResponseAcceptation) claimantResponse;
            final PaymentPlanMetadata claimantPaymentPlan = responseAcceptation.getClaimantPaymentIntention()
                .map(PaymentPlanMetadata::fromPaymentIntention)
                .orElse(null);
            final PaymentPlanMetadata courtPaymentPlan = responseAcceptation.getCourtDetermination()
                .map(CourtDetermination::getCourtPaymentIntention)
                .map(PaymentPlanMetadata::fromPaymentIntention)
                .orElse(null);
            final DecisionType courtDecision = responseAcceptation.getCourtDetermination()
                .map(CourtDetermination::getDecisionType)
                .orElse(null);
            final FormaliseOption formaliseOption = responseAcceptation.getFormaliseOption().orElse(null);

            return new ClaimantResponseMetadata(
                claimantRespondedAt,
                claimantResponseType,
                fullPaymentReceived,
                settleForAmount,
                claimantPaymentPlan,
                courtPaymentPlan,
                courtDecision,
                formaliseOption,
                null
            );
        }

        if (claimantResponse instanceof ResponseRejection) {
            final ResponseRejection responseRejection = (ResponseRejection) claimantResponse;
            final Optional<YesNoOption> optionalMediation = responseRejection.getFreeMediation();
            final Boolean mediation = optionalMediation.isPresent()
                ? optionalMediation.filter(isEqual(YesNoOption.YES)).isPresent()
                : null;
            return new ClaimantResponseMetadata(
                claimantRespondedAt,
                claimantResponseType,
                fullPaymentReceived,
                settleForAmount,
                null,
                null,
                null,
                null,
                mediation
            );
        }

        return null;
    }
}
