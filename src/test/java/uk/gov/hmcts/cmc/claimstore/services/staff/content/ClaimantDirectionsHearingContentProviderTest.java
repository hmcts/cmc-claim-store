package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.config.PebbleConfiguration;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.services.staff.ClaimantRejectionStaffNotificationService.getParameters;

public class ClaimantDirectionsHearingContentProviderTest {

    private final TemplateService templateService = new TemplateService(
        new PebbleConfiguration().pebbleEngine()
    );

    private final StaffEmailTemplates templates = new StaffEmailTemplates();

    private ClaimantDirectionsHearingContentProvider service;

    @Before
    public void beforeEachTest() {
        service = new ClaimantDirectionsHearingContentProvider(
            templateService,
            templates
        );
    }

    @Test
    public void shouldGenerateContentsForEmailBody() {
        Claim claim = SampleClaim.getDefault();
        EmailContent content = service.createContent(getParameters(claim));
        assertThat(content.getBody())
            .contains("The claimant has reviewed the defendant’s response and wish to proceed with the claim"
                + " if they opted into mediation the MILO team has been sent their contact & claim details.");
        assertThat(content.getBody())
            .contains("Please Log code 197 (DIRECTIONS QUESTIONNAIRE FILED) on Caseman.");
    }

    @Test
    public void shouldGenerateContentsForEmailSubject() {
        Claim claim = SampleClaim.getDefault();

        EmailContent content = service.createContent(getParameters(claim));
        String subject = String.format("%s %s v %s DQ submitted",
            claim.getReferenceNumber(),
            claim.getClaimData().getClaimant().getName(),
            claim.getClaimData().getDefendant().getName()
        );

        assertThat(content.getSubject()).isEqualTo(subject);
    }

}
