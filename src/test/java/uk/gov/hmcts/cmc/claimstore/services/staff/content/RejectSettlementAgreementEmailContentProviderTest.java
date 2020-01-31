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
import static uk.gov.hmcts.cmc.claimstore.services.staff.RejectSettlementAgreementStaffNotificationService.wrapInMap;

public class RejectSettlementAgreementEmailContentProviderTest {

    private final TemplateService templateService = new TemplateService(
        new PebbleConfiguration().pebbleEngine()
    );

    private final StaffEmailTemplates templates = new StaffEmailTemplates();

    private RejectSettlementAgreementEmailContentProvider service;

    @Before
    public void beforeEachTest() {
        service = new RejectSettlementAgreementEmailContentProvider(
            templateService,
            templates
        );
    }

    @Test
    public void shouldUseRequiredFieldsInTheSubject() {
        Claim claim = SampleClaim.getClaimWithSettlementAgreementRejected();

        EmailContent content = service.createContent(wrapInMap(claim));
        assertThat(content.getSubject())
            .contains(claim.getClaimData().getClaimant().getName())
            .contains(claim.getClaimData().getDefendant().getName());
    }

    @Test
    public void shouldDisplayAppropriateRejectionMessage() {
        Claim claim = SampleClaim.getClaimWithSettlementAgreementRejected();

        EmailContent content = service.createContent(wrapInMap(claim));

        assertThat(content.getBody())
            .contains("The defendant has rejected the claimant's offer to settle their claim.")
            .contains("This email has been sent from the HMCTS Civil Money Claims online court.");
    }

}
