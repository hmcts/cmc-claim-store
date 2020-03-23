package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.config.PebbleConfiguration;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.ScheduledStateTransitionContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.TemplateService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ScheduledStateTransitionContentProviderTest {

    private final TemplateService templateService = new TemplateService(
        new PebbleConfiguration().pebbleEngine()
    );

    private final StaffEmailTemplates templates = new StaffEmailTemplates();

    private ScheduledStateTransitionContentProvider contentProvider;

    @Before
    public void beforeEachTest() {
        contentProvider = new ScheduledStateTransitionContentProvider(
            templateService,
            templates
        );
    }

    @Test
    public void shouldGenerateContentsForEmailBody() {
        final List<Claim> claims = IntStream.range(1, 4)
            .mapToObj(i -> "000MC00" + i)
            .map(reference -> SampleClaim.getDefault().toBuilder().referenceNumber(reference).build())
            .collect(Collectors.toList());

        CaseEvent caseEvent = CaseEvent.STAY_CLAIM;

        EmailContent content = contentProvider.createContent(claims, caseEvent);

        String subject = String.format("Transitioning via %s failed for %s claims.\n"
            + "\n"
            + "Failed claims:\n"
            + "000MC001\n"
            + "000MC002\n"
            + "000MC003", caseEvent, claims.size());

        assertThat(content.getBody()).isEqualTo(subject);
    }

    @Test
    public void shouldGenerateContentsForEmailSubject() {

        final List<Claim> claims = Stream.generate(SampleClaim::getDefault).limit(3).collect(Collectors.toList());
        CaseEvent caseEvent = CaseEvent.STAY_CLAIM;

        EmailContent content = contentProvider.createContent(claims, caseEvent);

        String subject = String.format("Claims failed to transition via %s", caseEvent);

        assertThat(content.getSubject()).isEqualTo(subject);
    }

}
