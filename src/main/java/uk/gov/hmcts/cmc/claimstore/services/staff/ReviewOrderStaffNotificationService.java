package uk.gov.hmcts.cmc.claimstore.services.staff;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.ReviewOrderStaffEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.Collections;
import java.util.Map;

import static uk.gov.hmcts.cmc.domain.models.ReviewOrder.RequestedBy.CLAIMANT;

@Service
public class ReviewOrderStaffNotificationService {

    public static final String CLAIM_REFERENCE_NUMBER = "claimReferenceNumber";
    public static final String CLAIMANT_NAME = "claimantName";
    public static final String DEFENDANT_NAME = "defendantName";
    public static final String PARTY_NAME = "partyName";
    public static final String REVIEW_REASON = "reviewReason";

    private final EmailService emailService;
    private final StaffEmailProperties emailProperties;
    private final ReviewOrderStaffEmailContentProvider emailContentProvider;

    @Autowired
    public ReviewOrderStaffNotificationService(
        EmailService emailService,
        StaffEmailProperties emailProperties,
        ReviewOrderStaffEmailContentProvider emailContentProvider
    ) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.emailContentProvider = emailContentProvider;
    }

    public void notifyForReviewOrder(Claim claim) {
        EmailContent emailContent = emailContentProvider.createContent(wrapInMap(claim));
        emailService.sendEmail(
            emailProperties.getSender(),
            new EmailData(
                emailProperties.getRecipient(),
                emailContent.getSubject(),
                emailContent.getBody(),
                Collections.emptyList()
            )
        );
    }

    public static Map<String, Object> wrapInMap(Claim claim) {
        ReviewOrder reviewOrder = claim.getReviewOrder().orElseThrow(IllegalStateException::new);
        String claimantName = claim.getClaimData().getClaimant().getName();
        String defendantName = claim.getClaimData().getDefendant().getName();

        ImmutableMap.Builder<String, Object> builder = ImmutableMap.<String, Object>builder()
            .put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber())
            .put(CLAIMANT_NAME, claimantName)
            .put(DEFENDANT_NAME, defendantName)
            .put(PARTY_NAME, reviewOrder.getRequestedBy() == CLAIMANT ? claimantName : defendantName);

        reviewOrder.getReason().ifPresent(reason -> builder.put(REVIEW_REASON, reason));

        return builder.build();
    }
}
