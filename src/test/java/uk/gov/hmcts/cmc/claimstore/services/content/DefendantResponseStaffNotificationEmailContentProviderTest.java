package uk.gov.hmcts.cmc.claimstore.services.content;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.config.PebbleConfiguration;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.DefendantResponseStaffNotificationEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmccase.models.Claim;
import uk.gov.hmcts.cmccase.models.ResponseData;
import uk.gov.hmcts.cmccase.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmccase.models.sampledata.SampleParty;
import uk.gov.hmcts.cmccase.models.sampledata.SampleResponseData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.services.staff.DefendantResponseStaffNotificationService.wrapInMap;

public class DefendantResponseStaffNotificationEmailContentProviderTest {

    private static final String DEFENDANT_EMAIL = "defendant@mail.com";

    private TemplateService templateService = new TemplateService(
        new PebbleConfiguration().pebbleEngine()
    );

    private StaffEmailTemplates templates = new StaffEmailTemplates();

    private DefendantResponseStaffNotificationEmailContentProvider service;

    @Before
    public void beforeEachTest() {
        service = new DefendantResponseStaffNotificationEmailContentProvider(
            templateService,
            templates
        );
    }

    @Test
    public void shouldUseRequiredFieldsInTheSubject() {
        EmailContent content = service.createContent(wrapInMap(SampleClaim.getWithDefaultResponse(), DEFENDANT_EMAIL));
        assertThat(content.getSubject())
            .contains("Civil Money Claim defence submitted:")
            .contains("John Rambo v John Smith")
            .contains("000CM001");
    }

    @Test
    public void shouldUseRequiredFieldsInTheBody() {
        Claim claim = SampleClaim.getWithDefaultResponse();
        EmailContent content = service.createContent(wrapInMap(claim, DEFENDANT_EMAIL));
        assertThat(content.getBody())
            .contains("Email: " + DEFENDANT_EMAIL)
            .contains("Mobile number: " + claim.getResponse().orElseThrow(IllegalStateException::new).getDefendant()
                .getMobilePhone().orElseThrow(IllegalStateException::new));
    }

    @Test
    public void shouldDisplayAppropriateMessageWhenMobileNumberIsNotGiven() {
        Claim claim = SampleClaim.builder().withResponse(
            SampleResponseData.builder()
                .withDefendantDetails(
                    SampleParty.builder()
                        .withMobilePhone(null)
                        .individual())
                .build())
            .build();

        EmailContent content = service.createContent(wrapInMap(
            claim, DEFENDANT_EMAIL
        ));
        assertThat(content.getBody())
            .contains("Mobile number: not given");
    }

    @Test
    public void shouldUseFullDefenceTextIfFullDefenceSelected() {
        Claim claim = SampleClaim.builder().withResponse(
            SampleResponseData.builder()
                .withResponseType(ResponseData.ResponseType.OWE_NONE)
                .build())
            .build();

        EmailContent content = service.createContent(
            wrapInMap(claim, DEFENDANT_EMAIL)
        );
        assertThat(content.getBody())
            .contains("The defendant has submitted a full defence which is attached as a PDF.")
            .doesNotContain("The defendant has submitted an already paid defence which is attached as a PDF.")
            .doesNotContain("You need to ask the claimant if they want to proceed with the claim.");
    }

    @Test
    public void shouldUsePaidAllDefenceTextIfPaidAllDefenceSelected() {
        EmailContent content = service.createContent(wrapInMap(SampleClaim.getWithDefaultResponse(), DEFENDANT_EMAIL));
        assertThat(content.getBody())
            .contains("The defendant has submitted an already paid defence which is attached as a PDF.")
            .contains("You need to ask the claimant if they want to proceed with the claim.")
            .doesNotContain("The defendant has submitted a full defence which is attached as a PDF.");
    }

    @Test
    public void shouldUseFreeMediationTextIfFreeMediationIsRequested() {
        EmailContent content = service.createContent(
            wrapInMap(SampleClaim.getWithDefaultResponse(), DEFENDANT_EMAIL)
        );
        assertThat(content.getBody())
            .contains("The defendant has asked to use the mediation service")
            .doesNotContain("The defendant has chosen not to use the free mediation service.");
    }

    @Test
    public void shouldUseAlternativeTextIfFreeMediationIsNotRequested() {
        Claim claim = SampleClaim.builder().withResponse(
            SampleResponseData.builder()
                .withMediation(ResponseData.FreeMediationOption.NO)
                .build())
            .build();

        EmailContent content = service.createContent(
            wrapInMap(claim, DEFENDANT_EMAIL)
        );
        assertThat(content.getBody())
            .contains("The defendant has chosen not to use the free mediation service.")
            .doesNotContain("The defendant has asked to use the mediation service");
    }

    @Test
    public void shouldShowQuestionnaireTextIfMediationNotRequestedAndIsFullDefence() {
        Claim claim = SampleClaim.builder().withResponse(
            SampleResponseData.builder()
                .withResponseType(ResponseData.ResponseType.OWE_NONE)
                .withMediation(ResponseData.FreeMediationOption.NO)
                .build())
            .build();

        EmailContent content = service.createContent(
            wrapInMap(claim, DEFENDANT_EMAIL)
        );
        assertThat(content.getBody())
            .contains("You must progress to the directions questionnaire procedure.");
    }

    @Test
    public void shouldNowShowQuestionnaireTextIfMediationNotRequestedAndIsPaidall() {
        EmailContent content = service.createContent(
            wrapInMap(SampleClaim.getWithDefaultResponse(), DEFENDANT_EMAIL)
        );
        assertThat(content.getBody())
            .doesNotContain("You must progress to the directions questionnaire procedure.");
    }

}
