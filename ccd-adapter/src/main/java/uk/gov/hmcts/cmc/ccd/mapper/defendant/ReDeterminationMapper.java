package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDMadeBy;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

import static java.util.Objects.requireNonNull;

@Component
public class ReDeterminationMapper {
    public void to(CCDRespondent.CCDRespondentBuilder builder, Claim claim) {
        requireNonNull(builder, "builder must not be null");
        requireNonNull(claim, "claim must not be null");

        claim.getReDetermination().ifPresent(reDetermination -> {
            builder
                .reDeterminationExplanation(reDetermination.getExplanation())
                .reDeterminationMadeBy(CCDMadeBy.valueOf(reDetermination.getPartyType().name()));
        });

        claim.getReDeterminationRequestedAt().ifPresent(builder::reDeterminationRequestedDate);
    }

    public void from(Claim.ClaimBuilder builder, CCDRespondent respondent) {
        if (StringUtils.isBlank(respondent.getReDeterminationExplanation())
            && respondent.getReDeterminationMadeBy() == null
            && respondent.getReDeterminationRequestedDate() == null
        ) {
            return;
        }

        builder.reDetermination(ReDetermination.builder()
            .explanation(respondent.getReDeterminationExplanation())
            .partyType(MadeBy.valueOf(respondent.getReDeterminationMadeBy().name()))
            .build())
            .reDeterminationRequestedAt(respondent.getReDeterminationRequestedDate());

    }
}
