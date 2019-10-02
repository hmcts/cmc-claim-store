package uk.gov.hmcts.cmc.domain.models.metadata;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class RedeterminationMetadata {
    private final MadeBy madeBy;
    private final LocalDateTime date;

    static RedeterminationMetadata fromClaim(Claim claim) {
        final Optional<ReDetermination> optionalReDetermination = claim.getReDetermination();
        if (!optionalReDetermination.isPresent()) {
            return null;
        }
        final ReDetermination redetermination = optionalReDetermination.get();
        return new RedeterminationMetadata(
            redetermination.getPartyType(),
            claim.getReDeterminationRequestedAt().orElse(null)
        );
    }
}
