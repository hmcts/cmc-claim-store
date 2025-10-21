package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.PersonContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_REVIEW_ORDER;

@Component
public class ReviewOrderContentProvider {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final String CLAIM_REFERENCE_NUMBER = "claimReferenceNumber";
    private static final String PARTY_ADDRESS = "partyAddress";
    private static final String PARTY_FULL_NAME = "partyFullName";
    private static final String REVIEW_REASON = "reviewReason";
    private static final String REVIEW_DATE = "reviewDate";

    public Map<String, Object> createContent(Claim claim) {
        ReviewOrder reviewOrder = claim.getReviewOrder()
            .orElseThrow(() -> new IllegalStateException(MISSING_REVIEW_ORDER));
        PersonContent party = reviewOrder.getRequestedBy().equals(ReviewOrder.RequestedBy.DEFENDANT)
            ? PersonContent.builder()
                .address(claim.getClaimData().getDefendant().getAddress())
                .fullName(claim.getClaimData().getDefendant().getName()).build()
            : PersonContent.builder()
                .address(claim.getClaimData().getClaimant().getAddress())
                .fullName(claim.getClaimData().getClaimant().getName()).build();
        return createContent(
            claim.getReferenceNumber(),
            party,
            reviewOrder
        );
    }

    private Map<String, Object> createContent(String claimReferenceNumber,
                                              PersonContent party,
                                              ReviewOrder reviewOrder) {
        requireNonNull(claimReferenceNumber);
        requireNonNull(party);
        requireNonNull(reviewOrder);

        Map<String, Object> content = new HashMap<>();
        content.put(PARTY_FULL_NAME, party.getFullName());
        content.put(PARTY_ADDRESS, party.getAddress());
        content.put(REVIEW_DATE, reviewOrder.getRequestedAt().format(FORMATTER));
        content.put(REVIEW_REASON, reviewOrder.getReason().orElse("none provided"));
        content.put(CLAIM_REFERENCE_NUMBER, claimReferenceNumber);
        return content;
    }
}
