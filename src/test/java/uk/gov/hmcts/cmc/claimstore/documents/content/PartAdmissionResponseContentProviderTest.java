package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse.PartAdmission.builder;

public class PartAdmissionResponseContentProviderTest {

    private PartAdmissionResponseContentProvider provider =AdmissionValidator
        new PartAdmissionResponseContentProvider(
            new AdmissionContentProvider(
                new StatementOfMeansContentProvider())
        );

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        provider.createContent(null);
    }

    @Test
    public void shouldProvidePartAdmissionPaymentMadeDetails() {
        PartAdmissionResponse partAdmissionResponse = builder().build();
        Map<String, Object> content = provider.createContent(partAdmissionResponse);

        assertThat(content).containsKey("isAlreadyPaid");
        assertThat(content).containsKey("paymentDate");
        assertThat(content).containsKey("paymentMethod")
            .containsValue("Cash Payment");

        assertThat(content)
            .containsKey("responseTypeSelected")
            .containsValue(ResponseType.PART_ADMISSION.getDescription());

        assertThat(content)
            .containsKey("paidAmount")
            .containsValue("£120.00");

        assertThat(content).containsKey("events");
        assertThat(content).containsKey("evidences");
        assertThat(content).containsKey("evidenceComment");
        assertThat(content).containsKey("timelineComment");

        assertThat(content)
            .containsKey("responseDefence")
            .containsValue("defence string");

    }

    @Test
    public void shouldProvidePartAdmissionPaymentOptionImmediately() {
        PartAdmissionResponse partAdmissionResponse = builder().buildWithPaymentOptionImmediately();
        Map<String, Object> content = provider.createContent(partAdmissionResponse);

        assertThat(content)
            .containsKeys("paymentOption");
        assertThat(content).containsValues("Immediately");
    }

    @Test
    public void shouldProvidePartAdmissionPaymentOptionInstalments() {
        PartAdmissionResponse partAdmissionResponse = builder().buildWithPaymentOptionInstallments();
        Map<String, Object> content = provider.createContent(partAdmissionResponse);

        assertThat(content)
            .containsKeys("paymentOption");
        assertThat(content).containsValues("By instalments");
    }

    @Test
    public void shouldProvidePartAdmissionPaymentOptionBySetDate() {
        PartAdmissionResponse partAdmissionResponse = builder().buildWithPaymentOptionBySpecifiedDate();
        Map<String, Object> content = provider.createContent(partAdmissionResponse);

        assertThat(content)
            .containsKeys("paymentOption");
        assertThat(content).containsValues("By a set date");
    }
}
