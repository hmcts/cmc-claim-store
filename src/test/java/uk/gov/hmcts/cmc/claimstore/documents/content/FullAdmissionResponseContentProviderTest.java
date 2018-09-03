package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse.FullAdmission.builder;

public class FullAdmissionResponseContentProviderTest {

    private FullAdmissionResponseContentProvider provider =
        new FullAdmissionResponseContentProvider(
            new PaymentIntentionContentProvider(),
            new StatementOfMeansContentProvider()
        );

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        provider.createContent(null);
    }

    @Test
    public void shouldProvideFullAdmissionPaymentOptionImmediately() {
        FullAdmissionResponse fullAdmissionResponse = builder().build();
        Map<String, Object> content = provider.createContent(fullAdmissionResponse);

        assertThat(content)
            .containsKeys("paymentOption");
        assertThat(content).containsValues("By instalments");
    }

    @Test
    public void shouldProvideFullAdmissionPaymentOptionInstalments() {
        FullAdmissionResponse fullAdmissionResponse = builder().buildWithPaymentOptionImmediately();
        Map<String, Object> content = provider.createContent(fullAdmissionResponse);

        assertThat(content)
            .containsKeys("paymentOption");
        assertThat(content).containsValues("Immediately");
    }

    @Test
    public void shouldProvideFullAdmissionPaymentOptionBySetDate() {
        FullAdmissionResponse fullAdmissionResponse = builder().buildWithPaymentOptionBySpecifiedDate();
        Map<String, Object> content = provider.createContent(fullAdmissionResponse);

        assertThat(content)
            .containsKeys("paymentOption");
        assertThat(content).containsValues("By a set date");
    }

    @Test
    public void shouldProvideCorrectFormNumber() {
        FullAdmissionResponse fullAdmissionResponse = builder().buildWithPaymentOptionBySpecifiedDate();
        Map<String, Object> content = provider.createContent(fullAdmissionResponse);
        assertThat(content).containsKey("formNumber");
        assertThat(content).containsValue("OCON9A");
    }
}
