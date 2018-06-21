package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class FullAdmissionResponseContentProviderTest {

    private FullAdmissionResponse fullAdmissionResponse = SampleResponse.FullAdmission.builder().build();
    private FullAdmissionResponse fullAdmissionResponseWithInstalments = SampleResponse.FullAdmission.builder().buildWithInstalments();
    private FullAdmissionResponse fullAdmissionResponseWithSetDate = SampleResponse.FullAdmission.builder().buildWithSpecifiedDate();


    private FullAdmissionResponseContentProvider provider = new FullAdmissionResponseContentProvider(
            new StatementOfMeansContentProvider()
    );

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        provider.createContent(null);
    }

    @Test
    public void shouldProvideFullAdmissionPaymentOptionImmediately() {
        Map<String, Object> content = provider.createContent(fullAdmissionResponse);

        assertThat(content)
            .containsKeys("paymentOption");
        assertThat(content).containsValues("Immediately");
    }

    @Test
    public void shouldProvideFullAdmissionPaymentOptionInstalments() {
        Map<String, Object> content = provider.createContent(fullAdmissionResponseWithInstalments);

        assertThat(content)
            .containsKeys("paymentOption");
        assertThat(content).containsValues("By instalments");
    }

    @Test
    public void shouldProvideFullAdmissionPaymentOptionBySetDate() {
        Map<String, Object> content = provider.createContent(fullAdmissionResponseWithSetDate);

        assertThat(content)
            .containsKeys("paymentOption");
        assertThat(content).containsValues("By a set date");
    }
}
