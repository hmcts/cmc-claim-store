package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.config.PebbleConfiguration;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.IntentionToProceedDeadlineContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.documents.content.IntentionToProceedDeadlineContentProvider.getParameters;

public class IntentionToProceedDeadlineContentProviderTest {

    private TemplateService templateService = new TemplateService(
        new PebbleConfiguration().pebbleEngine()
    );

    private StaffEmailTemplates templates = new StaffEmailTemplates();

    private IntentionToProceedDeadlineContentProvider service;

    @Before
    public void beforeEachTest() {
        service = new IntentionToProceedDeadlineContentProvider(
            templateService,
            templates
        );
    }

    @Test
    public void shouldGenerateContentsForEmailBody() {
        LocalDate now = LocalDate.now();
        Claim claim = buildClaim(now);
        EmailContent content = service.createContent(getParameters(claim));

        assertThat(content.getBody())
            .contains(String.format("%s has said that they intend to proceed with their claim against %s.",
                claim.getClaimData().getClaimant().getName(),
                claim.getClaimData().getDefendant().getName()
                ));
        assertThat(content.getBody())
            .contains(String.format("Both parties need to submit their hearing requirements before 4pm on %s.",
                now.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))));
        assertThat(content.getBody())
            .contains("This email has been sent out from the HMCTS Civil Money Claims online court.");
    }

    @Test
    public void shouldGenerateContentsForEmailSubject() {
        LocalDate now = LocalDate.now();
        Claim claim = buildClaim(now);

        EmailContent content = service.createContent(getParameters(claim));
        String subject = String.format("Intention to proceed %s: %s v %s",
            claim.getReferenceNumber(),
            claim.getClaimData().getClaimant().getName(),
            claim.getClaimData().getDefendant().getName()
        );

        assertThat(content.getSubject()).isEqualTo(subject);
    }

    private Claim buildClaim(LocalDate now) {
        return SampleClaim.getDefault()
            .toBuilder()
            .directionsQuestionnaireDeadline(now)
            .build();
    }
}
