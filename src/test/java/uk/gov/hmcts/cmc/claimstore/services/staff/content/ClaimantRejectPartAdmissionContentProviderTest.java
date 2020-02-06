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
import static uk.gov.hmcts.cmc.claimstore.services.staff.ClaimantRejectionStaffNotificationService.wrapInMap;

public class ClaimantRejectPartAdmissionContentProviderTest {

    private final TemplateService templateService = new TemplateService(
        new PebbleConfiguration().pebbleEngine()
    );

    private final StaffEmailTemplates templates = new StaffEmailTemplates();

    private ClaimantRejectPartAdmissionContentProvider service;

    @Before
    public void beforeEachTest() {
        service = new ClaimantRejectPartAdmissionContentProvider(
            templateService,
            templates
        );
    }

    @Test
    public void shouldUseRequiredFieldsInTheBody() {
        Claim claim = SampleClaim.getWithClaimantResponseRejectionForPartAdmissionAndMediation();
        EmailContent content = service.createContent(wrapInMap(claim));
        assertThat(content.getBody())
            .contains(claim.getClaimData().getClaimant().getName() + " has rejected a partial admission");
    }

    @Test
    public void shouldUseBothPartyMediationTextIfBothPartiesAgreeToMediation() {
        Claim claim = SampleClaim.getWithClaimantResponseRejectionForPartAdmissionAndMediation();
        EmailContent content = service.createContent(wrapInMap(claim));
        assertThat(content.getBody())
            .contains("Both parties have agreed for a mediation.");
    }

    @Test
    public void shouldUseBothPartyDQTextIfBothPartiesDoNotAgreeToMediation() {
        Claim claim = SampleClaim.getWithClaimantResponseRejectionForPartAdmissionNoMediation();

        EmailContent content = service.createContent(wrapInMap(claim));
        assertThat(content.getBody())
            .contains("Both parties rejected mediation, and have requested for Direction Questionnaires to proceed.");

    }

}
