package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.CountyCourtJudgmentAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.DefendantLinkingException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ResponseAlreadySubmittedException;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SamplePaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DEFENDANT_OPTED_OUT_FOR_MEDIATION_PILOT;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DEFENDANT_OPTED_OUT_FOR_NON_MEDIATION_PILOT;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_FULL_ADMISSION_SUBMITTED_IMMEDIATELY;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_FULL_ADMISSION_SUBMITTED_INSTALMENTS;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_FULL_ADMISSION_SUBMITTED_SET_DATE;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_FULL_DEFENCE_SUBMITTED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_FULL_DEFENCE_SUBMITTED_STATES_PAID;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_PART_ADMISSION_SUBMITTED_IMMEDIATELY;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_PART_ADMISSION_SUBMITTED_INSTALMENTS;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_PART_ADMISSION_SUBMITTED_SET_DATE;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_PART_ADMISSION_SUBMITTED_STATES_PAID;
import static uk.gov.hmcts.cmc.claimstore.utils.DirectionsQuestionnaireUtils.DQ_FLAG;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.EXTERNAL_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.USER_ID;
import static uk.gov.hmcts.cmc.domain.utils.FeaturesUtils.MEDIATION_PILOT;

@RunWith(MockitoJUnitRunner.class)
public class DefendantResponseServiceTest {

    private static final Response VALID_APP = SampleResponse.FullDefence.builder().withMediation(NO).build();
    private static final Claim claim = SampleClaim.getDefault();
    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String DEFENDANT_EMAIL = "test@example.com";

    private DefendantResponseService responseService;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private UserService userService;

    @Mock
    private ClaimService claimService;

    @Mock
    private AppInsights appInsights;

    @Before
    public void setup() {
        responseService = new DefendantResponseService(
            eventProducer,
            claimService,
            userService,
            appInsights
        );
    }

    @Test
    public void shouldCreateDefenceResponseEventWhenNoMediation() {
        //given
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(
            SampleUserDetails.getDefault()
        );
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(
            SampleUserDetails.builder().withUserId(USER_ID).withMail(DEFENDANT_EMAIL).build());

        Claim claim = SampleClaim.builder()
            .withResponse(VALID_APP)
            .withFeatures(ImmutableList.of(MEDIATION_PILOT))
            .build();
        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), anyString())).thenReturn(claim);

        //when
        responseService.save(EXTERNAL_ID, DEFENDANT_ID, VALID_APP, AUTHORISATION);

        //then
        verify(eventProducer, once())
            .createDefendantResponseEvent(eq(claim), anyString());

        verify(appInsights, once()).trackEvent(eq(RESPONSE_FULL_DEFENCE_SUBMITTED),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));

        verify(appInsights, once()).trackEvent(eq(DEFENDANT_OPTED_OUT_FOR_MEDIATION_PILOT),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));

    }

    @Test
    public void shouldRaiseAppInsightForNonMediationPilot() {
        //given
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(
            SampleUserDetails.getDefault()
        );
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(
            SampleUserDetails.builder().withUserId(USER_ID).withMail(DEFENDANT_EMAIL).build());

        Claim claim = SampleClaim.builder()
            .withResponse(VALID_APP)
            .withFeatures(ImmutableList.of(DQ_FLAG))
            .build();
        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), anyString())).thenReturn(claim);

        //when
        responseService.save(EXTERNAL_ID, DEFENDANT_ID, VALID_APP, AUTHORISATION);

        //then
        verify(appInsights, once()).trackEvent(eq(DEFENDANT_OPTED_OUT_FOR_NON_MEDIATION_PILOT),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));

    }

    @Test(expected = DefendantLinkingException.class)
    public void saveShouldThrowDefendantLinkingExceptionWhenClaimIsLinkedToOtherDefendant() {

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), anyString()))
            .thenReturn(SampleClaim.builder().withDefendantId("not-mine-claim").build());

        responseService.save(EXTERNAL_ID, DEFENDANT_ID, VALID_APP, AUTHORISATION);
    }

    @Test(expected = DefendantLinkingException.class)
    public void saveShouldThrowDefendantLinkingExceptionWhenClaimIsNotLinkedToAnyUser() {

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), anyString()))
            .thenReturn(SampleClaim.builder().withDefendantId(null).build());

        responseService.save(EXTERNAL_ID, DEFENDANT_ID, VALID_APP, AUTHORISATION);
    }

    @Test(expected = DefendantLinkingException.class)
    public void saveShouldThrowDefendantLinkingExceptionWhenClaimDefendantIdIsNullAndGivenDefendantIdIsNull() {

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), anyString()))
            .thenReturn(SampleClaim.builder().withDefendantId(null).build());

        responseService.save(EXTERNAL_ID, null, VALID_APP, AUTHORISATION);
    }

    @Test(expected = ResponseAlreadySubmittedException.class)
    public void saveShouldThrowResponseAlreadySubmittedExceptionWhenResponseSubmitted() {

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), anyString()))
            .thenReturn(SampleClaim.builder().withRespondedAt(LocalDateTime.now()).build());

        responseService.save(EXTERNAL_ID, DEFENDANT_ID, VALID_APP, AUTHORISATION);
    }

    @Test(expected = CountyCourtJudgmentAlreadyRequestedException.class)
    public void saveShouldThrowCountyCourtJudgmentAlreadyRequestedExceptionWhenCCJRequested() {

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), anyString()))
            .thenReturn(SampleClaim.builder().withCountyCourtJudgmentRequestedAt(LocalDateTime.now()).build());

        responseService.save(EXTERNAL_ID, DEFENDANT_ID, VALID_APP, AUTHORISATION);
    }

    @Test
    public void getAppInsightsEventNameShouldReturnFullDefence() {
        Response response = SampleResponse.FullDefence.builder().build();

        assertThat(responseService.getAppInsightsEventName(response))
            .isEqualTo(RESPONSE_FULL_DEFENCE_SUBMITTED);
    }

    @Test
    public void getAppInsightsEventNameShouldReturnFullDefenceStatesPaid() {
        Response response = SampleResponse.FullDefence
            .builder()
            .withDefenceType(DefenceType.ALREADY_PAID)
            .withMediation(NO)
            .build();

        assertThat(responseService.getAppInsightsEventName(response))
            .isEqualTo(RESPONSE_FULL_DEFENCE_SUBMITTED_STATES_PAID);
    }

    @Test
    public void getAppInsightsEventNameShouldReturnFullAdmissionForImmediatePayment() {
        Response response = SampleResponse.FullAdmission.builder().buildWithPaymentOptionImmediately();
        assertThat(responseService.getAppInsightsEventName(response))
            .isEqualTo(RESPONSE_FULL_ADMISSION_SUBMITTED_IMMEDIATELY);
    }

    @Test
    public void getAppInsightsEventNameShouldReturnFullAdmissionForSetByDatePayment() {
        Response response = SampleResponse.FullAdmission.builder().buildWithPaymentOptionBySpecifiedDate();
        assertThat(responseService.getAppInsightsEventName(response))
            .isEqualTo(RESPONSE_FULL_ADMISSION_SUBMITTED_SET_DATE);
    }

    @Test
    public void getAppInsightsEventNameShouldReturnFullAdmissionForInstalmentPayment() {
        Response response = SampleResponse.FullAdmission.builder().buildWithPaymentOptionInstalments();
        assertThat(responseService.getAppInsightsEventName(response))
            .isEqualTo(RESPONSE_FULL_ADMISSION_SUBMITTED_INSTALMENTS);
    }

    @Test
    public void getAppInsightsEventNameShouldReturnPartAdmissionForImmediatePayment() {
        Response response = SampleResponse.PartAdmission.builder().buildWithPaymentOptionImmediately();
        assertThat(responseService.getAppInsightsEventName(response))
            .isEqualTo(RESPONSE_PART_ADMISSION_SUBMITTED_IMMEDIATELY);
    }

    @Test
    public void getAppInsightsEventNameShouldReturnPartAdmissionForSetByDatePayment() {
        Response response = SampleResponse.PartAdmission.builder().buildWithPaymentOptionBySpecifiedDate();
        assertThat(responseService.getAppInsightsEventName(response))
            .isEqualTo(RESPONSE_PART_ADMISSION_SUBMITTED_SET_DATE);
    }

    @Test
    public void getAppInsightsEventNameShouldReturnPartAdmissionForInstalmentPayment() {
        Response response = SampleResponse.PartAdmission.builder().buildWithPaymentOptionInstalments();
        assertThat(responseService.getAppInsightsEventName(response))
            .isEqualTo(RESPONSE_PART_ADMISSION_SUBMITTED_INSTALMENTS);
    }

    @Test
    public void getAppInsightsEventNameShouldReturnPartAdmissionForPartAdmissionStatesPaid() {
        Response response = PartAdmissionResponse.builder()
            .paymentDeclaration(SamplePaymentDeclaration.builder().build()).build();
        assertThat(responseService.getAppInsightsEventName(response))
            .isEqualTo(RESPONSE_PART_ADMISSION_SUBMITTED_STATES_PAID);
    }

    @Test(expected = NullPointerException.class)
    public void getAppInsightsEventNameShouldThrowNullPointerExceptionForNullInput() {
        responseService.getAppInsightsEventName(null);
    }
}
