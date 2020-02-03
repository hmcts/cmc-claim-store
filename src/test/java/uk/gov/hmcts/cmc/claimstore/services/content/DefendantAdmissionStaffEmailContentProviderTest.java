package uk.gov.hmcts.cmc.claimstore.services.content;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.config.PebbleConfiguration;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.DefendantAdmissionStaffEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.services.staff.DefendantResponseStaffNotificationService.wrapInMap;

public class DefendantAdmissionStaffEmailContentProviderTest {

    private static final String DEFENDANT_EMAIL = "defendant@mail.com";

    private final TemplateService templateService = new TemplateService(
        new PebbleConfiguration().pebbleEngine()
    );

    private final StaffEmailTemplates templates = new StaffEmailTemplates();

    private DefendantAdmissionStaffEmailContentProvider service;

    @Before
    public void beforeEachTest() {
        service = new DefendantAdmissionStaffEmailContentProvider(
            templateService,
            templates
        );
    }

    @Test
    public void shouldUsePaymentOptionByImmediatelyForFullAdmission() {
        Claim claimWithFullAdmission = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().buildWithPaymentOptionImmediately())
            .withRespondedAt(LocalDateTime.now())
            .build();
        EmailContent content = service.createContent(wrapInMap(claimWithFullAdmission, DEFENDANT_EMAIL));
        assertThat(content.getSubject())
            .contains("Pay immediately 000CM001")
            .contains("John Rambo v Dr. John Smith");

        assertThat(content.getBody())
            .contains("The defendant has offered to pay immediately in response to the ")

            .contains("money claim made against them by John Rambo.");
    }

    @Test
    public void shouldUsePaymentOptionByInstalmentsForFullAdmission() {
        Claim claimWithFullAdmission = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().build())
            .withRespondedAt(LocalDateTime.now())
            .build();
        EmailContent content = service.createContent(wrapInMap(claimWithFullAdmission, DEFENDANT_EMAIL));

        assertThat(content.getSubject())
            .contains("Pay by instalments 000CM001 - John Rambo v Dr. John Smith");
        assertThat(content.getBody())
            .contains("The defendant has offered to pay by instalments in response to the ")
            .contains("money claim made against them by John Rambo.");
    }

    @Test
    public void shouldUsePaymentOptionBySetDateForFullAdmission() {
        Claim claimWithFullAdmission = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().buildWithPaymentOptionBySpecifiedDate())
            .withRespondedAt(LocalDateTime.now())
            .build();
        EmailContent content = service.createContent(wrapInMap(claimWithFullAdmission, DEFENDANT_EMAIL));

        assertThat(content.getSubject())
            .contains("Pay by a set date 000CM001 - John Rambo v Dr. John Smith");
        assertThat(content.getBody())
            .contains("The defendant has offered to pay by a set date in response to the ")
            .contains("money claim made against them by John Rambo.");
    }

    @Test
    public void shouldUsePaymentOptionByImmediatelyForPartAdmission() {
        Claim claimWithPartAdmission = SampleClaim.builder()
            .withResponse(SampleResponse.PartAdmission.builder().buildWithPaymentOptionImmediately())
            .withRespondedAt(LocalDateTime.now())
            .build();
        EmailContent content = service.createContent(wrapInMap(claimWithPartAdmission, DEFENDANT_EMAIL));
        assertThat(content.getSubject())
            .contains("Pay immediately 000CM001")
            .contains("John Rambo v Dr. John Smith");

        assertThat(content.getBody())
            .contains("The defendant has offered to pay immediately in response to the ")

            .contains("money claim made against them by John Rambo.");
    }

    @Test
    public void shouldUsePaymentOptionByInstalmentsForPartAdmission() {
        Claim claimWithPartAdmission = SampleClaim.builder()
            .withResponse(SampleResponse.PartAdmission.builder().buildWithPaymentOptionInstalments())
            .withRespondedAt(LocalDateTime.now())
            .build();
        EmailContent content = service.createContent(wrapInMap(claimWithPartAdmission, DEFENDANT_EMAIL));

        assertThat(content.getSubject())
            .contains("Pay by instalments 000CM001 - John Rambo v Dr. John Smith");
        assertThat(content.getBody())
            .contains("The defendant has offered to pay by instalments in response to the ")
            .contains("money claim made against them by John Rambo.");
    }

    @Test
    public void shouldUsePaymentOptionBySetDateForPartAdmission() {
        Claim claimWithPartAdmission = SampleClaim.builder()
            .withResponse(SampleResponse.PartAdmission.builder().buildWithPaymentOptionBySpecifiedDate())
            .withRespondedAt(LocalDateTime.now())
            .build();
        EmailContent content = service.createContent(wrapInMap(claimWithPartAdmission, DEFENDANT_EMAIL));

        assertThat(content.getSubject())
            .contains("Pay by a set date 000CM001 - John Rambo v Dr. John Smith");
        assertThat(content.getBody())
            .contains("The defendant has offered to pay by a set date in response to the ")
            .contains("money claim made against them by John Rambo.");
    }
}
