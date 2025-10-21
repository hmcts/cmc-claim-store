package uk.gov.hmcts.cmc.domain.models.metadata;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Predicate;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class DefendantResponseMetadata {
    private final LocalDateTime respondedAt;
    private final ResponseType responseType;
    private final StatesPaidMetadata statesPaid;
    private final PaymentPlanMetadata paymentPlan;
    private final Boolean mediation;

    static DefendantResponseMetadata fromClaim(Claim claim) {
        Optional<Response> optionalResponse = claim.getResponse();
        if (!optionalResponse.isPresent()) {
            return null;
        }

        Response response = optionalResponse.get();

        return new DefendantResponseMetadata(
            claim.getRespondedAt(),
            response.getResponseType(),
            StatesPaidMetadata.fromClaim(claim),
            PaymentPlanMetadata.fromResponse(response),
            response.getFreeMediation().isPresent()
                ? response.getFreeMediation().filter(Predicate.isEqual(YesNoOption.YES)).isPresent()
                : null
        );
    }
}
