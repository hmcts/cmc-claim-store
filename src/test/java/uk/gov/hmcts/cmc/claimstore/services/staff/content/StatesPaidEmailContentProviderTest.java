package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.config.PebbleConfiguration;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.services.staff.StatesPaidStaffNotificationService.wrapInMap;

public class StatesPaidEmailContentProviderTest {
    private static final String DEFENDANT_EMAIL = "defendant@mail.com";
    private static final String DEFENDANT_MOBILE = "07980111222";

    private TemplateService templateService = new TemplateService(
        new PebbleConfiguration().pebbleEngine()
    );

    private StaffEmailTemplates templates = new StaffEmailTemplates();

    private StatesPaidEmailContentProvider service;

    @Before
    public void beforeEachTest() {
        service = new StatesPaidEmailContentProvider(
            templateService,
            templates
        );
    }

    @Test
    public void shouldUseRequiredFieldsInTheBody() {
        Claim claim = SampleClaim.builder()
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withResponse(
                SampleResponse.FullDefence
                    .builder()
                    .withDefendantDetails(SampleParty.builder().withMobilePhone(DEFENDANT_MOBILE).individual())
                    .build())
            .withClaimantResponse(SampleClaimantResponse.validDefaultAcceptation())
            .build();
        EmailContent content = service.createContent(wrapInMap(claim));
        assertThat(content.getBody())
            .contains("Email: " + DEFENDANT_EMAIL)
            .contains("Mobile number: " + claim.getResponse().orElseThrow(IllegalStateException::new).getDefendant()
                .getMobilePhone().orElseThrow(IllegalStateException::new));
    }

    @Test
    public void shouldDisplayAppropriateMessageWhenMobileNumberIsNotGiven() {
        Claim claim = SampleClaim.builder()
            .withResponse(
                SampleResponse.FullDefence.builder()
                    .withDefendantDetails(SampleParty.builder().withMobilePhone("").individual())
                    .build())
            .withClaimantResponse(SampleClaimantResponse.validDefaultAcceptation())
            .build();

        EmailContent content = service.createContent(wrapInMap(claim));
        assertThat(content.getBody())
            .contains("Mobile number: not given");
    }

    @Test
    public void shouldUsePaidAllDefenceTextIfPaidAllDefenceSelected() {
        Claim claim = SampleClaim.builder()
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withResponse(
                SampleResponse.FullDefence
                    .builder()
                    .withDefenceType(DefenceType.ALREADY_PAID)
                    .withMediation(null)
                    .withDefendantDetails(SampleParty.builder().withMobilePhone(DEFENDANT_MOBILE).individual())
                    .build())
            .withClaimantResponse(SampleClaimantResponse.validDefaultAcceptation())
            .build();

        EmailContent content = service.createContent(wrapInMap(claim));
        assertThat(content.getBody())
            .contains("The defendant has submitted a States Paid defence.");
    }

    @Test
    public void shouldDisplayAppropriateClaimantResponseAcceptation() {
        Claim claim = SampleClaim.builder()
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withResponse(
                SampleResponse.FullDefence
                    .builder()
                    .withDefenceType(DefenceType.ALREADY_PAID)
                    .withMediation(null)
                    .withDefendantDetails(SampleParty.builder().withMobilePhone(DEFENDANT_MOBILE).individual())
                    .build())
            .withClaimantResponse(SampleClaimantResponse.validDefaultAcceptation())
            .build();

        EmailContent content = service.createContent(wrapInMap(claim));
        assertThat(content.getBody())
            .contains("The claimant has stated that the claim is: ACCEPTATION")
            .contains("Claimant has accepted the states paid defence. Please enter a code 73");
    }

    @Test
    public void shouldDisplayAppropriateClaimantResponseRejection() {
        Claim claim = SampleClaim.builder()
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withResponse(
                SampleResponse.FullDefence
                    .builder()
                    .withDefenceType(DefenceType.ALREADY_PAID)
                    .withMediation(null)
                    .withDefendantDetails(SampleParty.builder().withMobilePhone(DEFENDANT_MOBILE).individual())
                    .build()
            )
            .withClaimantResponse(SampleClaimantResponse.validDefaultRejection())
            .build();

        EmailContent content = service.createContent(wrapInMap(claim));

        assertThat(content.getBody())
            .contains("The claimant has stated that the claim is: REJECTION")
            .contains("Claimant has rejected the states paid defence. Please enter a code 67 & 196");
    }

}
