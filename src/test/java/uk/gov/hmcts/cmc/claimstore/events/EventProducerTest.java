package uk.gov.hmcts.cmc.claimstore.events;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmccase.models.Claim;
import uk.gov.hmcts.cmccase.models.ClaimData;
import uk.gov.hmcts.cmccase.models.offers.MadeBy;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIM;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIMANT_EMAIL;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.DEFENDANT_EMAIL;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.PIN;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleMoreTimeRequestedEvent.NEW_RESPONSE_DEADLINE;
import static uk.gov.hmcts.cmccase.models.sampledata.SampleClaim.USER_ID;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.SUBMITTER_NAME;

public class EventProducerTest {
    private static final String AUTHORISATION = "Bearer: aaa";

    private final UserDetails userDetails
        = SampleUserDetails.builder().withUserId(USER_ID).withMail(CLAIMANT_EMAIL).build();

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
        //given
        final ClaimIssuedEvent expectedEvent = new ClaimIssuedEvent(CLAIM, PIN, SUBMITTER_NAME);
        when(userService.getUserDetails(eq(AUTHORISATION))).thenReturn(userDetails);

        //when
        eventProducer.createClaimIssuedEvent(CLAIM, PIN, userDetails.getFullName());

        //then
        verify(publisher).publishEvent(any(ClaimIssuedEvent.class));
    }

    @Test
    public void shouldCreateClaimIssueEventWithRepresentedClaimant() throws Exception {

        ClaimData data = mock(ClaimData.class);
        Claim claim = mock(Claim.class);
        when(claim.getClaimData()).thenReturn(data);
        when(data.isClaimantRepresented()).thenReturn(true);

        //when
        eventProducer.createClaimIssuedEvent(claim, PIN, userDetails.getFullName());

        //then
        verify(publisher).publishEvent(any(RepresentedClaimIssuedEvent.class));
    }

    @Test
    public void shouldCreateDefendantResponseEvent() throws Exception {
        //given
        final DefendantResponseEvent expectedEvent
            = new DefendantResponseEvent(CLAIM);

        //when
        eventProducer.createDefendantResponseEvent(CLAIM);

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
    public void shouldCreateCountyCourtJudgmentSubmittedEvent() throws Exception {

        // given
        CountyCourtJudgmentRequestedEvent expectedEvent = new CountyCourtJudgmentRequestedEvent(CLAIM);

        // when
        eventProducer.createCountyCourtJudgmentRequestedEvent(CLAIM);

        //then
        verify(publisher).publishEvent(eq(expectedEvent));
    }

    @Test
    public void shouldCreateOfferMadeEventEvent() throws Exception {

        // given
        OfferMadeEvent expectedEvent = new OfferMadeEvent(CLAIM);

        // when
        eventProducer.createOfferMadeEvent(CLAIM);

        //then
        verify(publisher).publishEvent(eq(expectedEvent));
    }

    @Test
    public void shouldCreateOfferAcceptedEventEvent() throws Exception {

        // given
        OfferAcceptedEvent expectedEvent = new OfferAcceptedEvent(CLAIM, MadeBy.CLAIMANT);

        // when
        eventProducer.createOfferAcceptedEvent(CLAIM, MadeBy.CLAIMANT);

        //then
        verify(publisher).publishEvent(eq(expectedEvent));
    }
}
