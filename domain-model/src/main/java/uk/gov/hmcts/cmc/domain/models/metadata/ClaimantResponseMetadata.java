package uk.gov.hmcts.cmc.domain.models.metadata;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.function.Predicate.isEqual;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
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

    private static ClaimantResponseMetadataBuilder builder() {
        return new ClaimantResponseMetadataBuilder();
    }

    static ClaimantResponseMetadata fromClaim(Claim claim) {
        final Optional<ClaimantResponse> optionalClaimantResponse = claim.getClaimantResponse();
        if (!optionalClaimantResponse.isPresent()) {
            return null;
        }

        final ClaimantResponse claimantResponse = optionalClaimantResponse.get();
        final LocalDateTime claimantRespondedAt = claim.getClaimantRespondedAt().orElse(null);
        final Boolean settleForAmount = extractBooleanOrNull(claimantResponse::getSettleForAmount, YesNoOption.YES);
        final Boolean fullPaymentReceived = extractBooleanOrNull(
            claimantResponse::getAmountPaid,
            claim.getTotalAmountTillToday().orElse(null)
        );
        final ClaimantResponseType claimantResponseType = claimantResponse.getType();

        switch (claimantResponseType) {
            case ACCEPTATION:
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

                return ClaimantResponseMetadata.builder()
                    .claimantRespondedAt(claimantRespondedAt)
                    .claimantResponseType(claimantResponseType)
                    .fullPaymentReceived(fullPaymentReceived)
                    .settleForAmount(settleForAmount)
                    .claimantPaymentPlan(claimantPaymentPlan)
                    .courtPaymentPlan(courtPaymentPlan)
                    .courtDecision(courtDecision)
                    .formaliseOption(formaliseOption)
                    .build();

            case REJECTION:
                final ResponseRejection responseRejection = (ResponseRejection) claimantResponse;
                final Boolean mediation = extractBooleanOrNull(responseRejection::getFreeMediation, YesNoOption.YES);
                return ClaimantResponseMetadata.builder()
                    .claimantRespondedAt(claimantRespondedAt)
                    .claimantResponseType(claimantResponseType)
                    .fullPaymentReceived(fullPaymentReceived)
                    .settleForAmount(settleForAmount)
                    .mediation(mediation)
                    .build();

            default:
                return null;
        }
    }

    private static <T> Boolean extractBooleanOrNull(Supplier<Optional<T>> supplier, T compare) {
        Optional<T> optional = supplier.get();
        return optional.isPresent()
            ? optional.filter(isEqual(compare)).isPresent()
            : null;
    }
}
