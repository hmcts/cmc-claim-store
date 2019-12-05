package uk.gov.hmcts.cmc.claimstore.deprecated.services.staff;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.deprecated.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimIssuedStaffNotificationEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.services.staff.ClaimIssuedStaffNotificationService.wrapInMap;

public class ClaimIssuedStaffNotificationEmailContentProviderTest extends MockSpringTest {

    @Autowired
    ClaimIssuedStaffNotificationEmailContentProvider provider;

    @Test
    public void shouldFormatEmailSubjectToExpectedValue() {
        EmailContent emailContent = provider.createContent(wrapInMap(SampleClaim.getDefault()));
        assertThat(emailContent.getSubject()).isEqualTo("Claim 000CM001 issued");
    }

    @Test
    public void shouldFormatEmailSubjectToExpectedValueForLegal() {
        EmailContent emailContent = provider.createContent(wrapInMap(SampleClaim.getDefaultForLegal()));
        assertThat(emailContent.getSubject()).isEqualTo("Claim form 012LR345");
    }

    @Test
    public void shouldFormatEmailBodyToExpectedValue() {
        EmailContent emailContent = provider.createContent(wrapInMap(SampleClaim.getDefault()));
        assertThat(emailContent.getBody()).isEqualTo("Please find attached claim.");
    }
}
