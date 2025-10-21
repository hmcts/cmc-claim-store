package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse.FullAdmission.builder;

public class FullAdmissionResponseContentProviderTest {

    private final FullAdmissionResponseContentProvider provider =
        new FullAdmissionResponseContentProvider(
            new PaymentIntentionContentProvider(),
            new StatementOfMeansContentProvider()
        );

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        provider.createContent(null, BigDecimal.ZERO);
    }

    @Test
    public void shouldProvideFullAdmissionPaymentOptionImmediately() {
        FullAdmissionResponse fullAdmissionResponse = builder().build();
        Map<String, Object> content = provider.createContent(fullAdmissionResponse, BigDecimal.valueOf(200L));

        assertThat(content)
            .containsKeys("paymentOption");
        assertThat(content).containsValues("By instalments");
    }

    @Test
    public void shouldProvideFullAdmissionPaymentOptionInstalments() {
        FullAdmissionResponse fullAdmissionResponse = builder().buildWithPaymentOptionImmediately();
        Map<String, Object> content = provider.createContent(fullAdmissionResponse, BigDecimal.valueOf(2000.89));

        assertThat(content)
            .containsKeys("paymentOption");
        assertThat(content).containsValues("Immediately");
    }

    @Test
    public void shouldProvideFullAdmissionPaymentOptionBySetDate() {
        FullAdmissionResponse fullAdmissionResponse = builder().buildWithPaymentOptionBySpecifiedDate();
        Map<String, Object> content = provider.createContent(fullAdmissionResponse, BigDecimal.valueOf(800));

        assertThat(content)
            .containsKeys("paymentOption");
        assertThat(content).containsValues("By a set date");
    }

    @Test
    public void shouldProvideCorrectFormNumber() {
        FullAdmissionResponse fullAdmissionResponse = builder().buildWithPaymentOptionBySpecifiedDate();
        Map<String, Object> content = provider.createContent(fullAdmissionResponse, BigDecimal.valueOf(900));
        assertThat(content).containsKey("formNumber");
        assertThat(content).containsValue("OCON9A");
    }

    @Test
    public void shouldProvideFullAmountInContentMap() {
        FullAdmissionResponse fullAdmissionResponse = builder().buildWithPaymentOptionBySpecifiedDate();
        Map<String, Object> content = provider.createContent(fullAdmissionResponse, BigDecimal.valueOf(900));
        assertThat(content).containsKey("whenWillTheyFinishPaying");
        String whenWillTheyFinishPaying = (String)content.get("whenWillTheyFinishPaying");
        assertThat(whenWillTheyFinishPaying).startsWith("£900, no later");

        content = provider.createContent(fullAdmissionResponse, BigDecimal.valueOf(2000));
        whenWillTheyFinishPaying = (String)content.get("whenWillTheyFinishPaying");
        assertThat(whenWillTheyFinishPaying).startsWith("£2,000, no later");
    }
}
