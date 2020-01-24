package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.junit.Test;
import org.springframework.util.Assert;
import uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire.HearingContentProvider;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse.PartAdmission.builder;

public class PartAdmissionResponseContentProviderTest {

    private final PartAdmissionResponseContentProvider provider =
        new PartAdmissionResponseContentProvider(
            new PaymentIntentionContentProvider(),
            new StatementOfMeansContentProvider(),
            new HearingContentProvider()
        );

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        //noinspection ConstantConditions
        provider.createContent(null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldProvidePartAdmissionPaymentMadeDetails() {
        PartAdmissionResponse partAdmissionResponse = builder().build();
        Map<String, Object> content = provider.createContent(partAdmissionResponse);

        assertThat(content).containsKey("paymentDeclarationIsPresent");
        assertThat(content).containsKey("paymentDate");
        assertThat(content).containsKey("paymentMethod")
            .containsValue("Paid cash");

        assertThat(content)
            .containsKey("responseTypeSelected")
            .containsValue(DefenceType.ALREADY_PAID.getDescription());

        assertThat(content)
            .containsKey("amount")
            .containsValue("£120.99");

        assertThat(content).containsKey("events");
        assertThat(content).containsKey("evidences");
        assertThat(content).containsKey("evidenceComment");
        assertThat(content).containsKey("timelineComment");

        assertThat(content)
            .containsKey("responseDefence");
        assertThat(content.get("responseDefence"))
            .isInstanceOf(List.class);
        assertThat((List<String>) content.get("responseDefence"))
            .contains(SampleResponse.USER_DEFENCE);
        assertThat(content)
            .containsKey("paymentIntentionIsPresent")
            .containsValues(false);
    }

    @Test
    public void shouldProvidePartAdmissionPaymentOptionImmediately() {
        PartAdmissionResponse partAdmissionResponse = builder().buildWithPaymentOptionImmediately();
        Map<String, Object> content = provider.createContent(partAdmissionResponse);

        assertThat(content)
            .containsKey("responseTypeSelected")
            .containsValue(ResponseType.PART_ADMISSION.getDescription());
        assertThat(content)
            .containsKeys("paymentOption")
            .containsValues("Immediately");
        assertThat(content)
            .containsKeys("paymentIntentionIsPresent")
            .containsValues(true);
    }

    @Test
    public void shouldProvidePartAdmissionPaymentOptionInstalments() {
        PartAdmissionResponse partAdmissionResponse = builder().buildWithPaymentOptionInstalments();
        Map<String, Object> content = provider.createContent(partAdmissionResponse);

        assertThat(content)
            .containsKey("responseTypeSelected")
            .containsValue(ResponseType.PART_ADMISSION.getDescription());
        assertThat(content)
            .containsKeys("paymentOption")
            .containsValues("By instalments");
    }

    @Test
    public void shouldProvidePartAdmissionPaymentOptionBySetDate() {
        PartAdmissionResponse partAdmissionResponse = builder().buildWithPaymentOptionBySpecifiedDate();
        Map<String, Object> content = provider.createContent(partAdmissionResponse);

        assertThat(content)
            .containsKeys("paymentOption")
            .containsValues("By a set date");
    }

    @Test
    public void shouldProvideCorrectFormNumber() {
        PartAdmissionResponse partAdmissionResponse = builder().buildWithPaymentOptionInstalments();
        Map<String, Object> content = provider.createContent(partAdmissionResponse);
        assertThat(content).containsKey("formNumber");
        assertThat(content).containsValue("OCON9A");
    }

    @Test
    public void shouldProvideHearingContent() {
        PartAdmissionResponse partAdmissionResponse = builder().buildWithDirectionsQuestionnaire();
        Map<String, Object> content = provider.createContent(partAdmissionResponse);
        assertThat(content).containsKey("hearingContent");
        Assert.notNull(content.get("hearingContent"), "Hearing Content cannot be null");
    }
}
