package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CallbackHandlerFactory;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.MoreTimeRequestedCallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor.DrawOrderCallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor.GenerateOrderCallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DRAW_ORDER;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.GENERATE_ORDER;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.RESPONSE_MORE_TIME;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SEALED_CLAIM_UPLOAD;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.LEGAL_ADVISOR;

@RunWith(MockitoJUnitRunner.class)
public class CallbackHandlerFactoryTest {

    public static final String BEARER_TOKEN = "Bearer Token";
    @Mock
    private MoreTimeRequestedCallbackHandler moreTimeRequestedCallbackHandler;
    @Mock
    private DrawOrderCallbackHandler drawOrderCallbackHandler;
    @Mock
    private GenerateOrderCallbackHandler generateOrderCallbackHandler;
    @Mock
    private CallbackResponse callbackResponse;
    @Mock
    private UserService userService;

    private CallbackHandlerFactory callbackHandlerFactory;

    @Before
    public void setUp() {
        doCallRealMethod().when(moreTimeRequestedCallbackHandler).handledEvents();
        doCallRealMethod().when(moreTimeRequestedCallbackHandler).register(anyMap());
        doCallRealMethod().when(generateOrderCallbackHandler).handledEvents();
        doCallRealMethod().when(generateOrderCallbackHandler).register(anyMap());
        doCallRealMethod().when(drawOrderCallbackHandler).handledEvents();
        doCallRealMethod().when(drawOrderCallbackHandler).register(anyMap());
        callbackHandlerFactory = new CallbackHandlerFactory(
            ImmutableList.of(
                moreTimeRequestedCallbackHandler,
                generateOrderCallbackHandler,
                drawOrderCallbackHandler),
            userService);
    }

    @Test
    public void shouldDispatchCallbackForMoreTimeRequested() {
        Role supportedRole = CASEWORKER;
        UserDetails userDetails = SampleUserDetails.builder()
            .withRoles(supportedRole.getRole())
            .build();
        when(userService.getUserDetails(eq(BEARER_TOKEN))).thenReturn(userDetails);

        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(RESPONSE_MORE_TIME.getValue())
            .build();
        CallbackParams params = CallbackParams.builder()
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
        when(moreTimeRequestedCallbackHandler.handle(params)).thenReturn(callbackResponse);
        when(moreTimeRequestedCallbackHandler.getSupportedRoles()).thenReturn(ImmutableList.of(supportedRole));

        callbackHandlerFactory.dispatch(params);

        verify(moreTimeRequestedCallbackHandler).handle(params);
        verify(userService).getUserDetails(eq(BEARER_TOKEN));
    }

    @Test
    public void shouldDispatchCallbackForGenerateOrder() {
        Role supportedRole = LEGAL_ADVISOR;
        UserDetails userDetails = SampleUserDetails.builder()
            .withRoles(supportedRole.getRole())
            .build();
        when(userService.getUserDetails(eq(BEARER_TOKEN))).thenReturn(userDetails);

        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(GENERATE_ORDER.getValue())
            .build();
        CallbackParams params = CallbackParams.builder()
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
        when(generateOrderCallbackHandler.handle(params)).thenReturn(callbackResponse);
        when(generateOrderCallbackHandler.getSupportedRoles()).thenReturn(ImmutableList.of(supportedRole));

        callbackHandlerFactory.dispatch(params);

        verify(generateOrderCallbackHandler).handle(params);
        verify(userService).getUserDetails(eq(BEARER_TOKEN));
    }

    @Test
    public void shouldDispatchCallbackForDrawOrder() {
        Role supportedRole = LEGAL_ADVISOR;
        UserDetails userDetails = SampleUserDetails.builder()
            .withRoles(supportedRole.getRole())
            .build();
        when(userService.getUserDetails(eq(BEARER_TOKEN))).thenReturn(userDetails);

        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(DRAW_ORDER.getValue())
            .build();
        CallbackParams params = CallbackParams.builder()
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        when(drawOrderCallbackHandler.handle(params)).thenReturn(callbackResponse);
        when(drawOrderCallbackHandler.getSupportedRoles()).thenReturn(ImmutableList.of(supportedRole));

        callbackHandlerFactory.dispatch(params);

        verify(drawOrderCallbackHandler).handle(params);
        verify(userService).getUserDetails(eq(BEARER_TOKEN));
    }

    @Test
    public void shouldThrowIfUnsupportedEventForCallback() {
        try {
            CallbackRequest callbackRequest = CallbackRequest
                .builder()
                .eventId(SEALED_CLAIM_UPLOAD.getValue())
                .build();
            CallbackParams params = CallbackParams.builder()
                .request(callbackRequest)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
                .build();
            callbackHandlerFactory
                .dispatch(params);
            Assert.fail("Expected a CallbackException to be thrown");
        } catch (CallbackException expected) {
            assertThat(expected).hasMessage("Could not handle callback for event SealedClaimUpload");
        }
    }

    @Test
    public void shouldThrowIfUnknownEvent() {
        try {
            CallbackRequest callbackRequest = CallbackRequest
                .builder()
                .eventId("nope")
                .build();
            CallbackParams params = CallbackParams.builder()
                .request(callbackRequest)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
                .build();

            callbackHandlerFactory.dispatch(params);
            Assert.fail("Expected a CallbackException to be thrown");
        } catch (CallbackException expected) {
            assertThat(expected).hasMessage("Could not handle callback for event nope");
        }
    }

    @Test
    public void shouldThrowIfUserDoesNotHaveSupportedRoles() {
        try {
            UserDetails userDetails = SampleUserDetails.builder().withRoles("citizen").build();
            when(userService.getUserDetails(eq(BEARER_TOKEN))).thenReturn(userDetails);

            CallbackRequest callbackRequest = CallbackRequest
                .builder()
                .eventId(DRAW_ORDER.getValue())
                .build();
            CallbackParams params = CallbackParams.builder()
                .request(callbackRequest)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
                .build();

            when(drawOrderCallbackHandler.getSupportedRoles())
                .thenReturn(ImmutableList.of(LEGAL_ADVISOR));

            callbackHandlerFactory.dispatch(params);

            verify(userService).getUserDetails(eq(BEARER_TOKEN));

            Assert.fail("Expected a ForbiddenActionException to be thrown");
        } catch (ForbiddenActionException expected) {
            assertThat(expected).hasMessage("User does not have supported role for event DrawOrder");
        }
    }
}
