package uk.gov.hmcts.cmc.claimstore.events;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.models.DefaultJudgment;
import uk.gov.hmcts.cmc.claimstore.services.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.USER_ID;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIM;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIMANT_EMAIL;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.DEFENDANT_EMAIL;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.PIN;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleMoreTimeRequestedEvent.NEW_RESPONSE_DEADLINE;

public class EventProducerTest {
    private static final String AUTHORISATION = "Bearer: aaa";

    final UserDetails userDetails = new UserDetails(USER_ID, CLAIMANT_EMAIL);

    @Mock
    private UserService userService;
    @Mock
    private ApplicationEventPublisher publisher;

    private EventProducer eventProducer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        eventProducer = new EventProducer(publisher);
        when(userService.getUserDetails(eq(AUTHORISATION))).thenReturn(userDetails);
    }

    @Test
    public void shouldCreateClaimIssueEvent() throws Exception {

        runCreateClaimIssuedEventExpectEventObjectToBeCreated(CLAIM, ClaimIssuedEvent.class);
    }

    @Test
    public void shouldCreateClaimIssueEventWithRepresentedClaimant() throws Exception {

        ClaimData data = mock(ClaimData.class);
        Claim claim = mock(Claim.class);
        when(claim.getClaimData()).thenReturn(data);
        when(data.isClaimantRepresented()).thenReturn(true);

        runCreateClaimIssuedEventExpectEventObjectToBeCreated(claim, RepresentedClaimIssuedEvent.class);
    }

    private void runCreateClaimIssuedEventExpectEventObjectToBeCreated(Claim claim, Class expectedEventClass) {

        //when
        eventProducer.createClaimIssuedEvent(claim, PIN);

        //then
        verify(publisher).publishEvent(any(expectedEventClass));
    }

    @Test
    public void shouldCreateDefendantResponseEvent() throws Exception {
        //given

        final DefendantResponseEvent expectedEvent
            = new DefendantResponseEvent(CLAIM, DEFENDANT_RESPONSE);

        //when
        eventProducer.createDefendantResponseEvent(CLAIM, DEFENDANT_RESPONSE);

        //then
        verify(publisher).publishEvent(eq(expectedEvent));
    }

    @Test
    public void shouldCreateMoreTimeForResponseRequestEvent() throws Exception {
        //given
        final MoreTimeRequestedEvent expectedEvent
            = new MoreTimeRequestedEvent(CLAIM, NEW_RESPONSE_DEADLINE, DEFENDANT_EMAIL);

        //when
        eventProducer.createMoreTimeForResponseRequestedEvent(CLAIM, NEW_RESPONSE_DEADLINE, DEFENDANT_EMAIL);

        //then
        verify(publisher).publishEvent(eq(expectedEvent));
    }

    @Test
    public void shouldCreateDefaultJudgmentSubmittedEvent() throws Exception {

        // given
        DefaultJudgment defaultJudgment = mock(DefaultJudgment.class);
        DefaultJudgmentSubmittedEvent expectedEvent = new DefaultJudgmentSubmittedEvent(defaultJudgment, CLAIM);

        // when
        eventProducer.createDefaultJudgmentSubmittedEvent(defaultJudgment, CLAIM);

        //then
        verify(publisher).publishEvent(eq(expectedEvent));
    }
}
