package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.launchdarkly.client.LDUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimDataContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.InterestContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.BY_SPECIFIED_DATE;
import static uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention.bySetDate;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantResponseContentProviderTest {

    private PaymentIntentionContentProvider paymentIntentionContentProvider = new PaymentIntentionContentProvider();
    private InterestCalculationService interestCalculationService = new InterestCalculationService(Clock.systemUTC());
    private InterestContentProvider interestContentProvider = new InterestContentProvider(interestCalculationService);
    private NotificationsProperties notificationsProperties = new NotificationsProperties();

    private PartyDetailsContentProvider partyDetailsContentProvider = new PartyDetailsContentProvider();
    private ClaimDataContentProvider claimDataContentProvider
        = new ClaimDataContentProvider(interestContentProvider);
    private ResponseAcceptationContentProvider responseAcceptationContentProvider =
        new ResponseAcceptationContentProvider(paymentIntentionContentProvider);
    private ResponseRejectionContentProvider responseRejectionContentProvider = new ResponseRejectionContentProvider();

    private ClaimantResponseContentProvider contentProvider;
    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Before
    public void setup() {
        contentProvider = new ClaimantResponseContentProvider(
            partyDetailsContentProvider,
            claimDataContentProvider,
            notificationsProperties,
            responseAcceptationContentProvider,
            responseRejectionContentProvider,
            launchDarklyClient);
        when(launchDarklyClient.isFeatureEnabled(eq("redetermination-reason-in-pdf"), any(LDUser.class)))
            .thenReturn(true);
    }

    @Test
    public void shouldShowTextFullAdmission() {

        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().buildWithPaymentOptionInstalments())
            .withClaimantResponse(ResponseAcceptation.builder()
                .courtDetermination(CourtDetermination.builder()
                    .courtDecision(bySetDate())
                    .courtPaymentIntention(PaymentIntention.builder()
                        .paymentOption(BY_SPECIFIED_DATE)
                        .paymentDate(ResponseAcceptationContentProvider.SYSTEM_MAX_DATE)
                        .build())
                    .disposableIncome(BigDecimal.valueOf(-1))
                    .decisionType(DecisionType.COURT)
                    .build())
                .formaliseOption(FormaliseOption.CCJ)
                .build())
            .build();
        Map<String, Object> content = contentProvider.createContent(claim);

        assertThat(content).containsValues("I accept this amount");
    }

    @Test
    public void shouldShowAdmissionsTextFullAdmissionForReDetermination() {

        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().buildWithPaymentOptionInstalments())
            .withClaimantResponse(ResponseAcceptation.builder()
                .courtDetermination(CourtDetermination.builder()
                    .courtDecision(bySetDate())
                    .courtPaymentIntention(PaymentIntention.builder()
                        .paymentOption(BY_SPECIFIED_DATE)
                        .paymentDate(ResponseAcceptationContentProvider.SYSTEM_MAX_DATE)
                        .build())
                    .disposableIncome(BigDecimal.valueOf(-1))
                    .decisionType(DecisionType.COURT)
                    .build())
                .formaliseOption(FormaliseOption.CCJ)
                .build())
            .withReDetermination(new ReDetermination("because", MadeBy.DEFENDANT))
            .withReDeterminationRequestedAt(LocalDateTime.now())
            .build();

        Map<String, Object> content = contentProvider.createContent(claim);

        assertThat(content).containsValues("I accept full admission");
    }

    @Test
    public void shouldShowTextPartAdmission() {

        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.PartAdmission.builder().buildWithPaymentOptionInstalments())
            .withClaimantResponse(ResponseAcceptation.builder()
                .courtDetermination(CourtDetermination.builder()
                    .courtDecision(bySetDate())
                    .courtPaymentIntention(PaymentIntention.builder()
                        .paymentOption(BY_SPECIFIED_DATE)
                        .paymentDate(ResponseAcceptationContentProvider.SYSTEM_MAX_DATE)
                        .build())
                    .disposableIncome(BigDecimal.valueOf(-1))
                    .decisionType(DecisionType.COURT)
                    .build())
                .formaliseOption(FormaliseOption.CCJ)
                .build())
            .build();

        Map<String, Object> content = contentProvider.createContent(claim);

        assertThat(content).containsValues("I accept this amount");
    }

    @Test
    public void shouldShowAmountTextPartAdmissionForReDetermination() {

        Response response = SampleResponse.PartAdmission.builder().buildWithPaymentOptionInstalments();
        Claim claim = SampleClaim.builder()
            .withResponse(response)
            .withClaimantResponse(ResponseAcceptation.builder()
                .courtDetermination(CourtDetermination.builder()
                    .courtDecision(bySetDate())
                    .courtPaymentIntention(PaymentIntention.builder()
                        .paymentOption(BY_SPECIFIED_DATE)
                        .paymentDate(ResponseAcceptationContentProvider.SYSTEM_MAX_DATE)
                        .build())
                    .disposableIncome(BigDecimal.valueOf(-1))
                    .decisionType(DecisionType.COURT)
                    .build())
                .formaliseOption(FormaliseOption.CCJ)
                .build())
            .withReDetermination(new ReDetermination("because", MadeBy.DEFENDANT))
            .withReDeterminationRequestedAt(LocalDateTime.now())
            .build();

        Map<String, Object> content = contentProvider.createContent(claim);

        assertThat(content).containsValues(String.format("I accept %s",
            formatMoney(((PartAdmissionResponse) response).getAmount())));
    }

    @Test
    public void shouldShowFormaliseOptionForIndividuals() {
        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.PartAdmission.builder()
                .buildWithPaymentOptionInstalmentsAndParty(SampleParty.builder().individual()))
            .withClaimantResponse(ResponseAcceptation.builder()
                .courtDetermination(CourtDetermination.builder()
                    .courtDecision(bySetDate())
                    .courtPaymentIntention(PaymentIntention.builder()
                        .paymentOption(BY_SPECIFIED_DATE)
                        .paymentDate(ResponseAcceptationContentProvider.SYSTEM_MAX_DATE)
                        .build())
                    .disposableIncome(BigDecimal.valueOf(-1))
                    .decisionType(DecisionType.COURT)
                    .build())
                .formaliseOption(FormaliseOption.REFER_TO_JUDGE)
                .build())
            .build();

        Map<String, Object> content = contentProvider.createContent(claim);

        assertThat(content).containsValue("Refer to Judge");
    }

    @Test
    public void shouldShowFormaliseOptionForNonIndividuals() {
        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.PartAdmission.builder()
                .buildWithPaymentOptionInstalmentsAndParty(SampleParty.builder().company()))
            .withClaimantResponse(ResponseAcceptation.builder()
                .courtDetermination(CourtDetermination.builder()
                    .courtDecision(bySetDate())
                    .courtPaymentIntention(PaymentIntention.builder()
                        .paymentOption(BY_SPECIFIED_DATE)
                        .paymentDate(ResponseAcceptationContentProvider.SYSTEM_MAX_DATE)
                        .build())
                    .disposableIncome(BigDecimal.valueOf(-1))
                    .decisionType(DecisionType.COURT)
                    .build())
                .formaliseOption(FormaliseOption.REFER_TO_JUDGE)
                .build())
            .build();

        Map<String, Object> content = contentProvider.createContent(claim);

        assertThat(content).containsValue("Please enter judgment by determination");
    }

    @Test
    public void shouldShowRejectionReason() {

        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().buildWithPaymentOptionInstalments())
            .withClaimantResponse(ResponseAcceptation.builder()
                .courtDetermination(CourtDetermination.builder()
                    .rejectionReason("Rejection proposal")
                    .courtDecision(bySetDate())
                    .courtPaymentIntention(PaymentIntention.builder()
                        .paymentOption(BY_SPECIFIED_DATE)
                        .paymentDate(ResponseAcceptationContentProvider.SYSTEM_MAX_DATE)
                        .build())
                    .disposableIncome(BigDecimal.valueOf(-1))
                    .decisionType(DecisionType.COURT)
                    .build())
                .formaliseOption(FormaliseOption.CCJ)
                .build())
            .build();

        Map<String, Object> content = contentProvider.createContent(claim);

        assertThat(content).containsValues("Rejection proposal");
    }

    @Test
    public void shouldShowNoRejectionReason() {

        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().buildWithPaymentOptionInstalments())
            .withClaimantResponse(ResponseAcceptation.builder()
                .courtDetermination(CourtDetermination.builder()
                    .rejectionReason(null)
                    .courtDecision(bySetDate())
                    .courtPaymentIntention(PaymentIntention.builder()
                        .paymentOption(BY_SPECIFIED_DATE)
                        .paymentDate(ResponseAcceptationContentProvider.SYSTEM_MAX_DATE)
                        .build())
                    .disposableIncome(BigDecimal.valueOf(-1))
                    .decisionType(DecisionType.COURT)
                    .build())
                .formaliseOption(FormaliseOption.CCJ)
                .build())
            .build();

        Map<String, Object> content = contentProvider.createContent(claim);

        assertThat(content).doesNotContainKeys("rejectionReason");
    }

    @Test
    public void whenNoClaimantResponse() {

        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().buildWithPaymentOptionInstalments())
            .withClaimantResponse(null)
            .build();
        Assertions.assertThrows(IllegalStateException.class,
            () -> contentProvider.createContent(claim));
    }

    @Test
    public void shouldShowReasonForReDetermination() {

        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().buildWithPaymentOptionInstalments())
            .withReDetermination(ReDetermination.builder().explanation("reason").partyType(MadeBy.CLAIMANT).build())
            .withClaimantResponse(ResponseAcceptation.builder()
                .courtDetermination(CourtDetermination.builder()
                    .rejectionReason(null)
                    .courtDecision(bySetDate())
                    .courtPaymentIntention(PaymentIntention.builder()
                        .paymentOption(BY_SPECIFIED_DATE)
                        .paymentDate(ResponseAcceptationContentProvider.SYSTEM_MAX_DATE)
                        .build())
                    .disposableIncome(BigDecimal.valueOf(-1))
                    .decisionType(DecisionType.COURT)
                    .build())
                .formaliseOption(FormaliseOption.CCJ)
                .build())
            .build();

        Map<String, Object> content = contentProvider.createContent(claim);

        assertThat(content).containsValues("reason");
    }

    @Test
    public void shouldNotShowReasonForReDetermination() {

        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().buildWithPaymentOptionInstalments())
            .withReDetermination(ReDetermination.builder().explanation("").partyType(MadeBy.CLAIMANT).build())
            .withClaimantResponse(ResponseAcceptation.builder()
                .courtDetermination(CourtDetermination.builder()
                    .rejectionReason(null)
                    .courtDecision(bySetDate())
                    .courtPaymentIntention(PaymentIntention.builder()
                        .paymentOption(BY_SPECIFIED_DATE)
                        .paymentDate(ResponseAcceptationContentProvider.SYSTEM_MAX_DATE)
                        .build())
                    .disposableIncome(BigDecimal.valueOf(-1))
                    .decisionType(DecisionType.COURT)
                    .build())
                .formaliseOption(FormaliseOption.CCJ)
                .build())
            .build();

        Map<String, Object> content = contentProvider.createContent(claim);

        assertThat(content).containsValues("Not Provided");
    }

    @Test
    public void shouldNotShowReasonForReDeterminationToggleOff() {

        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().buildWithPaymentOptionInstalments())
            .withReDetermination(ReDetermination.builder().explanation("reason").partyType(MadeBy.CLAIMANT).build())
            .withClaimantResponse(ResponseAcceptation.builder()
                .courtDetermination(CourtDetermination.builder()
                    .rejectionReason(null)
                    .courtDecision(bySetDate())
                    .courtPaymentIntention(PaymentIntention.builder()
                        .paymentOption(BY_SPECIFIED_DATE)
                        .paymentDate(ResponseAcceptationContentProvider.SYSTEM_MAX_DATE)
                        .build())
                    .disposableIncome(BigDecimal.valueOf(-1))
                    .decisionType(DecisionType.COURT)
                    .build())
                .formaliseOption(FormaliseOption.CCJ)
                .build())
            .build();
        when(launchDarklyClient.isFeatureEnabled(eq("redetermination-reason-in-pdf"), any(LDUser.class)))
            .thenReturn(false);

        Map<String, Object> content = contentProvider.createContent(claim);

        assertThat(content).doesNotContainKeys("reasonForReDeterminationclaimant");
    }
}
