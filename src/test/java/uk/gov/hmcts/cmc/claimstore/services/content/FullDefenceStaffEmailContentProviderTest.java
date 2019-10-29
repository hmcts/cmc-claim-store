package uk.gov.hmcts.cmc.claimstore.services.content;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.config.PebbleConfiguration;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.FullDefenceStaffEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.services.staff.DefendantResponseStaffNotificationService.wrapInMap;

public class FullDefenceStaffEmailContentProviderTest {

    private static final String DEFENDANT_EMAIL = "defendant@mail.com";

    private TemplateService templateService = new TemplateService(
        new PebbleConfiguration().pebbleEngine()
    );

    private StaffEmailTemplates templates = new StaffEmailTemplates();

    private FullDefenceStaffEmailContentProvider service;

    @Before
    public void beforeEachTest() {
        service = new FullDefenceStaffEmailContentProvider(
            templateService,
            templates
        );
    }

    @Test
    public void shouldUseRequiredFieldsInTheSubject() {
        EmailContent content = service.createContent(wrapInMap(SampleClaim.getWithDefaultResponse(), DEFENDANT_EMAIL));
        assertThat(content.getSubject())
            .contains("Civil Money Claim defence submitted:")
            .contains("John Rambo v Dr. John Smith")
            .contains("000CM001");
    }

    @Test
    public void shouldUseRequiredFieldsInTheBody() {
        Claim claim = SampleClaim.getWithDefaultResponse();
        EmailContent content = service.createContent(wrapInMap(claim, DEFENDANT_EMAIL));
        assertThat(content.getBody())
            .contains("Email: " + DEFENDANT_EMAIL)
            .contains("Phone number: " + claim.getResponse().orElseThrow(IllegalStateException::new).getDefendant()
                .getPhone().orElseThrow(IllegalStateException::new));
    }

    @Test
    public void shouldDisplayAppropriateMessageWhenPhoneNumberIsNotGiven() {
        Claim claim = SampleClaim.builder().withResponse(
            SampleResponse.FullDefence.builder()
                .withDefendantDetails(
                    SampleParty.builder()
                        .withPhone(null)
                        .individual())
                .build())
            .build();

        EmailContent content = service.createContent(wrapInMap(
            claim, DEFENDANT_EMAIL
        ));
        assertThat(content.getBody())
            .contains("Phone number: not given");
    }

    @Test
    public void shouldUseFullDefenceTextIfFullDefenceSelected() {
        Claim claim = SampleClaim.builder().withResponse(
            SampleResponse.FullDefence.builder()
                .withDefenceType(DefenceType.DISPUTE)
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
        EmailContent content = service.createContent(
            wrapInMap(
                SampleClaim.builder()
                    .withResponse(
                        SampleResponse.FullDefence
                            .builder()
                            .withDefenceType(DefenceType.ALREADY_PAID)
                            .withMediation(null)
                            .build()
                    ).build(),
                DEFENDANT_EMAIL));
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
            SampleResponse.FullDefence.builder()
                .withMediation(YesNoOption.NO)
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
            SampleResponse.FullDefence.builder()
                .withDefenceType(DefenceType.DISPUTE)
                .withMediation(YesNoOption.NO)
                .build())
            .build();

        EmailContent content = service.createContent(
            wrapInMap(claim, DEFENDANT_EMAIL)
        );
        assertThat(content.getBody())
            .contains("You must progress to the directions questionnaire procedure.");
    }

    @Test
    public void shouldNotShowQuestionnaireTextIfMediationNotRequestedAndIsPaidAll() {
        EmailContent content = service.createContent(
            wrapInMap(
                SampleClaim.builder()
                    .withResponse(
                        SampleResponse.FullDefence.builder()
                            .withDefenceType(DefenceType.ALREADY_PAID)
                            .withMediation(null)
                            .build()
                    ).build(),
                DEFENDANT_EMAIL
            )
        );
        assertThat(content.getBody())
            .doesNotContain("You must progress to the directions questionnaire procedure.");
    }

}
