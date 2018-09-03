package uk.gov.hmcts.cmc.claimstore.events;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.OfferAcceptedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.OfferMadeEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.SignSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

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
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.USER_ID;

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
        when(userService.getUserDetails(eq(AUTHORISATION))).thenReturn(userDetails);

        //when
        eventProducer.createClaimIssuedEvent(CLAIM, PIN, userDetails.getFullName(), AUTHORISATION);

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
        eventProducer.createClaimIssuedEvent(claim, PIN, userDetails.getFullName(), AUTHORISATION);

        //then
        verify(publisher).publishEvent(any(RepresentedClaimIssuedEvent.class));
    }

    @Test
    public void shouldCreateDefendantResponseEvent() throws Exception {
        //given
        DefendantResponseEvent expectedEvent
            = new DefendantResponseEvent(CLAIM);

        //when
        eventProducer.createDefendantResponseEvent(CLAIM);

        //then
        verify(publisher).publishEvent(eq(expectedEvent));
    }

    @Test
    public void shouldCreateMoreTimeForResponseRequestEvent() throws Exception {

        //given
        MoreTimeRequestedEvent expectedEvent
            = new MoreTimeRequestedEvent(CLAIM, NEW_RESPONSE_DEADLINE, DEFENDANT_EMAIL);

        //when
        eventProducer.createMoreTimeForResponseRequestedEvent(CLAIM, NEW_RESPONSE_DEADLINE, DEFENDANT_EMAIL);

        //then
        verify(publisher).publishEvent(eq(expectedEvent));
    }

    @Test
    public void shouldCreateCountyCourtJudgmentSubmittedEvent() throws Exception {

        // given
        boolean issue = false;
        CountyCourtJudgmentEvent expectedEvent = new CountyCourtJudgmentEvent(CLAIM, AUTHORISATION, issue);
        // when
        eventProducer.createCountyCourtJudgmentEvent(CLAIM, AUTHORISATION, issue);

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

    @Test
    public void shouldCreateSignSettlementAgreementEvent() throws Exception {
        // given
        SignSettlementAgreementEvent event = new SignSettlementAgreementEvent(CLAIM);

        // when
        eventProducer.createSignSettlementAgreementEvent(CLAIM);

        //then
        verify(publisher).publishEvent(eq(event));
    }
}
