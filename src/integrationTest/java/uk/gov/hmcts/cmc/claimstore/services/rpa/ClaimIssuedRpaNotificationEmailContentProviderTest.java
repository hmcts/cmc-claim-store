package uk.gov.hmcts.cmc.claimstore.services.rpa;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.services.rpa.ClaimIssuedRpaNotificationService.wrapInMap;

public class ClaimIssuedRpaNotificationEmailContentProviderTest extends MockSpringTest {

    @Autowired
    ClaimIssuedRpaNotificationEmailContentProvider provider;

    @Test
    public void shouldFormatEmailSubjectToExpectedValue() {
        EmailContent emailContent = provider.createContent(wrapInMap(SampleClaim.getDefault()));
        assertThat(emailContent.getSubject()).isEqualTo("J new claim 000CM001");
    }

    @Test
    public void shouldFormatEmailBodyToExpectedValue() {
        EmailContent emailContent = provider.createContent(wrapInMap(SampleClaim.getDefault()));
        assertThat(emailContent.getBody()).isEqualTo("Please find attached claim.");
    }
}
