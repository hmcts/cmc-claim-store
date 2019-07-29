package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.config.PebbleConfiguration;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.services.staff.ReviewOrderStaffNotificationService.wrapInMap;
import static uk.gov.hmcts.cmc.domain.models.ReviewOrder.RequestedBy.CLAIMANT;
import static uk.gov.hmcts.cmc.domain.models.ReviewOrder.RequestedBy.DEFENDANT;

public class ReviewOrderStaffEmailContentProviderTest {
    public static final String REASON = "I disagree with the order";

    private TemplateService templateService = new TemplateService(
        new PebbleConfiguration().pebbleEngine()
    );

    private StaffEmailTemplates templates = new StaffEmailTemplates();

    private ReviewOrderStaffEmailContentProvider service;

    @Before
    public void beforeEachTest() {
        service = new ReviewOrderStaffEmailContentProvider(
            templateService,
            templates
        );
    }

    @Test
    public void shouldCreateValidContentsWhenReasonIsPresent() {
        ReviewOrder reviewOrder = ReviewOrder.builder()
            .reason(REASON)
            .requestedAt(now())
            .requestedBy(CLAIMANT)
            .build();

        Claim claim = SampleClaim.builder()
            .withReviewOrder(reviewOrder)
            .build();

        EmailContent content = service.createContent(wrapInMap(claim));

        assertThat(content.getBody())
            .contains(REASON)
            .contains(format("%s has requested a review of the Directions Order, please see their comments below",
                claim.getClaimData().getClaimant().getName()));

        assertThat((content.getSubject()))
            .isEqualTo(format("%s: %s v %s reconsideration request",
                claim.getReferenceNumber(),
                claim.getClaimData().getClaimant().getName(),
                claim.getClaimData().getDefendant().getName()
            ));
    }

    @Test
    public void shouldCreateValidContentsWhenReasonIsNotPresent() {
        ReviewOrder reviewOrder = ReviewOrder.builder()
            .requestedAt(now())
            .requestedBy(DEFENDANT)
            .build();

        Claim claim = SampleClaim.builder()
            .withReviewOrder(reviewOrder)
            .build();

        EmailContent content = service.createContent(wrapInMap(claim));

        assertThat(content.getBody())
            .contains("No supporting comments were entered by this party")
            .contains(format("%s has requested a review of the Directions Order, please see their comments below",
                claim.getClaimData().getDefendant().getName()));

        assertThat((content.getSubject()))
            .isEqualTo(format("%s: %s v %s reconsideration request",
                claim.getReferenceNumber(),
                claim.getClaimData().getClaimant().getName(),
                claim.getClaimData().getDefendant().getName()
            ));
    }
}
