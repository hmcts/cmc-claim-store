package uk.gov.hmcts.cmc.claimstore.services.content;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.config.PebbleConfiguration;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleDefendantResponse;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.DefendantResponse;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.DefendantResponseStaffNotificationEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.services.staff.DefendantResponseStaffNotificationService.wrapInMap;

public class DefendantResponseStaffNotificationEmailContentProviderTest {

    private static final String DEFENDANT_EMAIL = "defendant@mail.com";

    private TemplateService templateService = new TemplateService(
        new PebbleConfiguration().pebbleEngine()
    );

    private StaffEmailTemplates templates = new StaffEmailTemplates();

    private Claim claim;
    private DefendantResponse response;

    private DefendantResponseStaffNotificationEmailContentProvider service;

    @Before
    public void beforeEachTest() {
        claim = SampleClaim.getDefault();
        response = SampleDefendantResponse.getDefault();
        service = new DefendantResponseStaffNotificationEmailContentProvider(
            templateService,
            templates
        );
    }

    @Test
    public void shouldUseRequiredFieldsInTheSubject() {
        EmailContent content = service.createContent(wrapInMap(claim, response, DEFENDANT_EMAIL));
        assertThat(content.getSubject())
            .contains("Civil Money Claim defence submitted:")
            .contains("John Rambo v John Smith")
            .contains("000CM001");
    }

    @Test
    public void shouldUseRequiredFieldsInTheBody() {
        EmailContent content = service.createContent(wrapInMap(claim, response, DEFENDANT_EMAIL));
        assertThat(content.getBody())
            .contains("Email: " + DEFENDANT_EMAIL)
            .contains("Mobile number: " + response.getResponse().getDefendant().getMobilePhone().get());
    }

    @Test
    public void shouldDisplayAppropriateMessageWhenMobileNumberIsNotGiven() {
        EmailContent content = service.createContent(wrapInMap(
            claim, SampleDefendantResponse.getWithoutMobileNumber(), DEFENDANT_EMAIL
        ));
        assertThat(content.getBody())
            .contains("Mobile number: not given");
    }

    @Test
    public void shouldUseFullDefenceTextIfFullDefenceSelected() {
        EmailContent content = service.createContent(
            wrapInMap(claim, SampleDefendantResponse.getWithFullDefence(), DEFENDANT_EMAIL)
        );
        assertThat(content.getBody())
            .contains("The defendant has submitted a full defence which is attached as a PDF.")
            .doesNotContain("The defendant has submitted an already paid defence which is attached as a PDF.")
            .doesNotContain("You need to ask the claimant if they want to proceed with the claim.");
    }

    @Test
    public void shouldUsePaidAllDefenceTextIfPaidAllDefenceSelected() {
        EmailContent content = service.createContent(wrapInMap(claim, response, DEFENDANT_EMAIL));
        assertThat(content.getBody())
            .contains("The defendant has submitted an already paid defence which is attached as a PDF.")
            .contains("You need to ask the claimant if they want to proceed with the claim.")
            .doesNotContain("The defendant has submitted a full defence which is attached as a PDF.");
    }

    @Test
    public void shouldUseFreeMediationTextIfFreeMediationIsRequested() {
        EmailContent content = service.createContent(
            wrapInMap(claim, response, DEFENDANT_EMAIL)
        );
        assertThat(content.getBody())
            .contains("The defendant has asked to use the mediation service")
            .doesNotContain("The defendant has chosen not to use the free mediation service.");
    }

    @Test
    public void shouldUseAlternativeTextIfFreeMediationIsNotRequested() {
        EmailContent content = service.createContent(
            wrapInMap(claim, SampleDefendantResponse.getWithoutFreeMediation(), DEFENDANT_EMAIL)
        );
        assertThat(content.getBody())
            .contains("The defendant has chosen not to use the free mediation service.")
            .doesNotContain("The defendant has asked to use the mediation service");
    }

    @Test
    public void shouldShowQuestionnaireTextIfMediationNotRequestedAndIsFullDefence() {
        EmailContent content = service.createContent(
            wrapInMap(claim, SampleDefendantResponse.getWithoutFreeMediationFullDefence(), DEFENDANT_EMAIL)
        );
        assertThat(content.getBody())
            .contains("You must progress to the directions questionnaire procedure.");
    }

    @Test
    public void shouldNowShowQuestionnaireTextIfMediationNotRequestedAndIsPaidall() {
        EmailContent content = service.createContent(
            wrapInMap(claim, SampleDefendantResponse.getWithoutFreeMediation(), DEFENDANT_EMAIL)
        );
        assertThat(content.getBody())
            .doesNotContain("You must progress to the directions questionnaire procedure.");
    }

}
