package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.content.models.DefendantDetailsContent;
import uk.gov.hmcts.cmc.domain.models.ResponseData;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;

import static java.util.Objects.requireNonNull;

@Component
public class DefendantDetailsContentProvider {

    public DefendantDetailsContent createContent(
        final TheirDetails providedByClaimant,
        final ResponseData defendantResponse,
        final String defendantEmail
    ) {
        requireNonNull(providedByClaimant);
        requireNonNull(defendantResponse);
        return new DefendantDetailsContent(
            providedByClaimant,
            defendantResponse,
            defendantEmail
        );
    }
}
