package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.config.PebbleConfiguration;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse.ClaimantResponseRejection;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.services.staff.StatesPaidStaffNotificationService.wrapInMap;

public class StatesPaidEmailContentProviderTest {
    private static final String DEFENDANT_EMAIL = "defendant@mail.com";
    private static final String DEFENDANT_PHONE = "07980111222";

    private StatesPaidEmailContentProvider service;

    @Before
    public void beforeEachTest() {
        service = new StatesPaidEmailContentProvider(
            new TemplateService(new PebbleConfiguration().pebbleEngine()),
            new StaffEmailTemplates()
        );
    }

    @Test
    public void shouldUseRequiredFieldsInTheBody() {
        Claim claim = SampleClaim.builder()
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withResponse(
                SampleResponse.FullDefence
                    .builder()
                    .withDefendantDetails(SampleParty.builder().withPhone(DEFENDANT_PHONE).individual())
                    .build())
            .withClaimantResponse(SampleClaimantResponse.validDefaultAcceptation())
            .build();

        EmailContent content = service.createContent(wrapInMap(claim));
        assertThat(content.getBody())
            .contains("Email: " + DEFENDANT_EMAIL)
            .contains("Phone number: " + DEFENDANT_PHONE);
    }

    @Test
    public void shouldDisplayAppropriateMessageWhenPhoneNumberIsNotGiven() {
        Claim claim = SampleClaim.builder()
            .withResponse(
                SampleResponse.FullDefence.builder()
                    .withDefendantDetails(SampleParty.builder().withPhone("").individual())
                    .build())
            .withClaimantResponse(SampleClaimantResponse.validDefaultAcceptation())
            .build();

        EmailContent content = service.createContent(wrapInMap(claim));
        assertThat(content.getBody())
            .contains("Phone number: not given");
    }

    @Test
    public void shouldUsePaidAllDefenceTextIfPaidAllDefenceSelected() {
        Claim claim = SampleClaim.builder()
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withResponse(SampleResponse.FullDefence.builder().withDefenceType(DefenceType.ALREADY_PAID).build())
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
                    .withDefendantDetails(SampleParty.builder().withPhone(DEFENDANT_PHONE).individual())
                    .build())
            .withClaimantResponse(SampleClaimantResponse.validDefaultAcceptation())
            .build();

        EmailContent content = service.createContent(wrapInMap(claim));
        assertThat(content.getBody())
            .contains("Claimant has accepted the states paid defence.")
            .contains("Please enter a code 73");
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
                    .withDefendantDetails(SampleParty.builder().withPhone(DEFENDANT_PHONE).individual())
                    .build()
            )
            .withClaimantResponse(SampleClaimantResponse.validDefaultRejection())
            .build();

        EmailContent content = service.createContent(wrapInMap(claim));
        assertThat(content.getBody())
            .contains("Claimant has rejected the states paid defence.")
            .contains("Please enter a code 67");
    }

    @Test
    public void shouldDisplayMediationAgreed() {
        Claim claim = SampleClaim.builder()
            .withResponse(
                SampleResponse.FullDefence
                    .builder()
                    .withDefenceType(DefenceType.ALREADY_PAID)
                    .withMediation(YesNoOption.YES)
                    .build()
            )
            .withClaimantResponse(new ClaimantResponseRejection().buildRejectionWithFreeMediation())
            .build();

        EmailContent content = service.createContent(wrapInMap(claim));
        assertThat(content.getBody())
            .contains("Both parties have agreed to mediation.");
    }

}
