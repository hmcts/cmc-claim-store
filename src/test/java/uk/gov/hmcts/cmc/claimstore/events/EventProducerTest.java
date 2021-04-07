package uk.gov.hmcts.cmc.claimstore.events;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.BreathingSpaceEnteredEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.HwfClaimUpdatedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.OfferAcceptedEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.OfferMadeEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantPaperResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.events.revieworder.ReviewOrderEvent;
import uk.gov.hmcts.cmc.claimstore.events.settlement.RejectSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.events.settlement.SignSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.List;

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
    public static final String DEFENDANT_EMAIL_TEMPLATE = "Defendant Email Template";
    public static final String CLAIMANT_EMAIL_TEMPLATE = "Claimant Email Template";
    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String LETTER_TEMPLATEID = "CV-CMC-LET-ENG-00635.docx";
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
    public void shouldCreateClaimIssueEvent() {
        //given
        when(userService.getUserDetails(eq(AUTHORISATION))).thenReturn(userDetails);

        //when
        eventProducer.createClaimIssuedEvent(CLAIM, PIN, userDetails.getFullName(), AUTHORISATION);

        //then
        verify(publisher).publishEvent(any(ClaimIssuedEvent.class));
    }

    @Test
    public void shouldCreateHwfClaimUpdatedEvent() {
        //given
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);

        //when
        eventProducer.createHwfClaimUpdatedEvent(CLAIM, userDetails.getFullName(), AUTHORISATION);

        //then
        verify(publisher).publishEvent(any(HwfClaimUpdatedEvent.class));
    }

    @Test
    public void shouldCreateClaimIssueEventWithRepresentedClaimant() {

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
    public void shouldCreateDefendantResponseEvent() {
        //given
        DefendantResponseEvent expectedEvent
            = new DefendantResponseEvent(CLAIM, AUTHORISATION);

        //when
        eventProducer.createDefendantResponseEvent(CLAIM, AUTHORISATION);

        //then
        verify(publisher).publishEvent(eq(expectedEvent));
    }

    @Test
    public void shouldCreateDefendantPaperResponseEvent() {
        //given
        DefendantPaperResponseEvent expectedEvent
            = new DefendantPaperResponseEvent(CLAIM, AUTHORISATION);

        //when
        eventProducer.createDefendantPaperResponseEvent(CLAIM, AUTHORISATION);

        //then
        verify(publisher).publishEvent(eq(expectedEvent));
    }

    @Test
    public void shouldCreateMoreTimeForResponseRequestEvent() {

        //given
        MoreTimeRequestedEvent expectedEvent
            = new MoreTimeRequestedEvent(CLAIM, NEW_RESPONSE_DEADLINE, DEFENDANT_EMAIL);

        //when
        eventProducer.createMoreTimeForResponseRequestedEvent(CLAIM, NEW_RESPONSE_DEADLINE, DEFENDANT_EMAIL);

        //then
        verify(publisher).publishEvent(eq(expectedEvent));
    }

    @Test
    public void shouldCreateDefaultCountyCourtJudgmentSubmittedEvent() {
        //given
        CountyCourtJudgmentEvent expectedEvent = new CountyCourtJudgmentEvent(CLAIM, AUTHORISATION);
        // when
        eventProducer.createCountyCourtJudgmentEvent(CLAIM, AUTHORISATION);

        //then
        verify(publisher).publishEvent(eq(expectedEvent));
    }

    @Test
    public void shouldCreateOfferMadeEventEvent() {

        // given
        OfferMadeEvent expectedEvent = new OfferMadeEvent(CLAIM);

        // when
        eventProducer.createOfferMadeEvent(CLAIM);

        //then
        verify(publisher).publishEvent(eq(expectedEvent));
    }

    @Test
    public void shouldCreateOfferAcceptedEventEvent() {

        // given
        OfferAcceptedEvent expectedEvent = new OfferAcceptedEvent(CLAIM, MadeBy.CLAIMANT);

        // when
        eventProducer.createOfferAcceptedEvent(CLAIM, MadeBy.CLAIMANT);

        //then
        verify(publisher).publishEvent(eq(expectedEvent));
    }

    @Test
    public void shouldCreateSignSettlementAgreementEvent() {
        // given
        SignSettlementAgreementEvent event = new SignSettlementAgreementEvent(CLAIM);

        // when
        eventProducer.createSignSettlementAgreementEvent(CLAIM);

        //then
        verify(publisher).publishEvent(eq(event));
    }

    @Test
    public void shouldCreateRejectSettlementAgreementEvent() {
        RejectSettlementAgreementEvent event = new RejectSettlementAgreementEvent(CLAIM);

        eventProducer.createRejectSettlementAgreementEvent(CLAIM);

        verify(publisher).publishEvent(eq(event));
    }

    @Test
    public void shouldCreateReviewOrderEvent() {
        ReviewOrderEvent event = new ReviewOrderEvent(AUTHORISATION, CLAIM);

        eventProducer.createReviewOrderEvent(AUTHORISATION, CLAIM);

        verify(publisher).publishEvent(eq(event));
    }

    @Test
    public void shouldCreateBulkPrintTransferEvent() {

        Document coverLetter = mock(Document.class);
        List<BulkPrintTransferEvent.PrintableDocument> caseDocuments = mock(List.class);

        BulkPrintTransferEvent event = new BulkPrintTransferEvent(CLAIM, coverLetter, caseDocuments, AUTHORISATION);

        eventProducer.createBulkPrintTransferEvent(CLAIM, coverLetter, caseDocuments, AUTHORISATION);

        verify(publisher).publishEvent(eq(event));
    }

    @Test
    public void shouldCreateBreathingSpaceEnteredEvent() {

        eventProducer.createBreathingSpaceEnteredEvent(CLAIM, CCDCase.builder().build(), AUTHORISATION,
            LETTER_TEMPLATEID, CLAIMANT_EMAIL_TEMPLATE, DEFENDANT_EMAIL_TEMPLATE, true);

        verify(publisher).publishEvent(any(BreathingSpaceEnteredEvent.class));
    }

}
