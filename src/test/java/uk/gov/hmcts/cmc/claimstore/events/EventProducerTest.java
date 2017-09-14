package uk.gov.hmcts.cmc.claimstore.events;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.USER_ID;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIM;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIMANT_EMAIL;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.DEFENDANT_EMAIL;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.PIN;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.SUBMITTER_FORENAME;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.SUBMITTER_SURNAME;

public class EventProducerTest {
    private static final String AUTHORISATION = "Bearer: aaa";

    @Mock
    private UserService userService;
    @Mock
    private ApplicationEventPublisher publisher;

    private EventProducer eventProducer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        eventProducer = new EventProducer(publisher);
    }

    @Test
    public void shouldCreateClaimIssueEvent() throws Exception {
        //given
        final UserDetails userDetails = new UserDetails(USER_ID, CLAIMANT_EMAIL, SUBMITTER_FORENAME, SUBMITTER_SURNAME);
        final ClaimIssuedEvent expectedEvent = new ClaimIssuedEvent(CLAIM, PIN);
        when(userService.getUserDetails(eq(AUTHORISATION))).thenReturn(userDetails);

        //when
        eventProducer.createClaimIssuedEvent(CLAIM, PIN, userDetails.getFullName());

        //then
        verify(publisher).publishEvent(eq(expectedEvent));
    }

    @Test
    public void shouldCreateDefendantResponseEvent() throws Exception {
        //given
        final UserDetails userDetails
            = new UserDetails(USER_ID, DEFENDANT_EMAIL, SUBMITTER_FORENAME, SUBMITTER_SURNAME);

        final DefendantResponseEvent expectedEvent
            = new DefendantResponseEvent(CLAIM, DEFENDANT_RESPONSE);

        when(userService.getUserDetails(eq(AUTHORISATION))).thenReturn(userDetails);

        //when
        eventProducer.createDefendantResponseEvent(CLAIM, DEFENDANT_RESPONSE);

        //then
        verify(publisher).publishEvent(eq(expectedEvent));
    }
}
