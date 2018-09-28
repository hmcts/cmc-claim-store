package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.CCJContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;

import java.util.Collections;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Component
public class ContentProvider {

    private final ClaimContentProvider claimContentProvider;
    private final AmountContentProvider amountContentProvider;

    @Autowired
    public ContentProvider(ClaimContentProvider claimContentProvider, AmountContentProvider amountContentProvider) {
        this.claimContentProvider = claimContentProvider;
        this.amountContentProvider = amountContentProvider;
    }

    public Map<String, Object> createContent(Claim claim) {
        requireNonNull(claim);
        return Collections.singletonMap("ccj", new CCJContent(
            claimContentProvider.createContent(claim),
            claim.getCountyCourtJudgment(),
            claim.getCountyCourtJudgmentRequestedAt(),
            amountContentProvider.create(claim),
            admissionResponse(claim)
            )
        );
    }

    private boolean admissionResponse(Claim claim) {
        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);

        return response.getResponseType().equals(ResponseType.PART_ADMISSION)
            || response.getResponseType().equals(ResponseType.FULL_ADMISSION);
    }
}
