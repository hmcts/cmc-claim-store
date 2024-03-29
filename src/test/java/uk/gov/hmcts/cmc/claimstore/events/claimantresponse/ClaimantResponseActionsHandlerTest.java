package uk.gov.hmcts.cmc.claimstore.events.claimantresponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimantRejectionDefendantDocumentService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimantRejectionDefendantNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationToDefendantService;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimantRejectOrgPaymentPlanStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse.ClaimantResponseRejection;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse.PartAdmission;

import java.net.URI;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.ADMISSIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.DQ_FLAG;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantResponseActionsHandlerTest {
    private static final URI DOCUMENT_URI = URI.create("http://localhost/doc.pdf");
    private final String authorisation = "Bearer authorisation";
    private static final String DATE = "1999-01-01";
    private static final String DOCUMENT_URL = "http://test.url";
    private static final String DOCUMENT_BINARY_URL = "http://test.bin.url";
    private static final String DOCUMENT_FILE_NAME = "form.pdf";

    private static final CCDDocument DOCUMENT = CCDDocument
        .builder()
        .documentUrl(DOCUMENT_URL)
        .documentBinaryUrl(DOCUMENT_BINARY_URL)
        .documentFileName(DOCUMENT_FILE_NAME)
        .build();

    private ClaimantResponseActionsHandler handler;
    @Mock
    private NotificationToDefendantService notificationService;
    @Mock
    private ClaimantRejectionDefendantNotificationService defendantNotificationService;
    @Mock
    private ClaimantRejectOrgPaymentPlanStaffNotificationService claimantRejectOrgPaymentPlanStaffNotificationService;
    @Mock
    private ClaimantRejectionDefendantNotificationService claimantRejectionDefendantNotificationService;
    @Mock
    private ClaimantRejectionDefendantDocumentService claimantRejectionDefendantDocumentService;

    @Before
    public void setUp() {
        handler = new ClaimantResponseActionsHandler(
            notificationService,
            claimantRejectOrgPaymentPlanStaffNotificationService,
            claimantRejectionDefendantNotificationService,
            claimantRejectionDefendantDocumentService
            );
    }

    @Test
    public void sendNotificationToDefendantWhenFreeMediationConfirmed() {
        //given
        ClaimantResponse claimantResponse = ClaimantResponseRejection.validRejectionWithFreeMediation();
        Response response = SampleResponse.FullDefence.builder().withMediation(YES).build();
        Claim claim = SampleClaim.builder().withResponse(response).withClaimantResponse(claimantResponse).build();
        ClaimantResponseEvent event = new ClaimantResponseEvent(claim, authorisation);
        //when
        handler.sendNotificationToDefendant(event);
        //then
        verify(notificationService).notifyDefendantOfFreeMediationConfirmationByClaimant(eq(claim));
    }

    @Test
    public void sendNotificationToDefendantWhenClaimantHasSettledForFullDefense() {
        //given
        ClaimantResponse claimantResponse = ClaimantResponseRejection.validDefaultAcceptation();
        Response response = SampleResponse.FullDefence.builder().withMediation(YES).build();
        Claim claim = SampleClaim.builder().withResponse(response).withClaimantResponse(claimantResponse).build();
        ClaimantResponseEvent event = new ClaimantResponseEvent(claim, authorisation);
        //when
        handler.sendNotificationToDefendant(event);
        //then
        verify(notificationService).notifyDefendantOfClaimantSettling(eq(claim));
    }

    @Test
    public void sendNotificationToDefendantWhenClaimantHasIntentionToProceedForPaperDq() {
        //given
        ClaimantResponse claimantResponse = ClaimantResponseRejection.builder().build();
        Response response = SampleResponse.FullDefence.builder().withMediation(NO).build();
        Claim claim = SampleClaim.builder()
            .withResponse(response)
            .withClaimantResponse(claimantResponse)
            .withDirectionsQuestionnaireDeadline(LocalDate.now())
            .build();

        ClaimantResponseEvent event = new ClaimantResponseEvent(claim, authorisation);
        //when
        handler.sendNotificationToDefendant(event);
        //then
        verify(notificationService).notifyDefendantOfClaimantIntentionToProceedForPaperDq(eq(claim));
    }

    @Test
    public void sendNotificationToDefendantWhenClaimantHasIntentionToProceedForOnlineDq() {
        //given
        ClaimantResponse claimantResponse = ResponseRejection.builder()
            .freeMediation(NO)
            .directionsQuestionnaire(SampleDirectionsQuestionnaire.builder().build())
            .build();

        Response response = SampleResponse.FullDefence.builder()
            .withDirectionsQuestionnaire(SampleDirectionsQuestionnaire.builder().build())
            .withMediation(NO)
            .build();

        Claim claim = SampleClaim.builder()
            .withResponse(response)
            .withClaimantResponse(claimantResponse)
            .withFeatures(ImmutableList.of(DQ_FLAG.getValue(), ADMISSIONS.getValue()))
            .build();

        ClaimantResponseEvent event = new ClaimantResponseEvent(claim, authorisation);
        //when
        handler.sendNotificationToDefendant(event);
        //then
        verify(notificationService).notifyDefendantOfClaimantIntentionToProceedForOnlineDq(eq(claim));
    }

    @Test
    public void shouldNotSendNotificationToDefendantWhenFreeMediationNotConfirmed() {
        //given
        ClaimantResponse claimantResponse = ClaimantResponseRejection.validRejectionWithFreeMediation();
        Response response = SampleResponse.FullDefence.builder().withMediation(NO).build();
        Claim claim = SampleClaim.builder().withResponse(response).withClaimantResponse(claimantResponse).build();
        ClaimantResponseEvent event = new ClaimantResponseEvent(claim, authorisation);
        //when
        handler.sendNotificationToDefendant(event);
        //then
        verify(notificationService, never()).notifyDefendantOfFreeMediationConfirmationByClaimant(eq(claim));
    }

    @Test
    public void sendNotificationToDefendantOfResponse() {
        //given
        ClaimantResponse claimantResponse = ClaimantResponseRejection.validDefaultRejection();
        Response response = PartAdmission.builder().buildWithStatesPaid(SampleParty.builder().individual());
        Claim claim = SampleClaim.builder().withResponse(response).withClaimantResponse(claimantResponse).build();
        ClaimantResponseEvent event = new ClaimantResponseEvent(claim, authorisation);
        //when
        handler.sendNotificationToDefendant(event);
        //then
        verify(notificationService).notifyDefendantOfClaimantResponse(eq(claim));
    }

    @Test
    public void sendNotificationLetterToDefendantIfClaimantRejectsMediation() {
        //given
        ClaimantResponse claimantResponse = ClaimantResponseRejection.validRejectionWithRejectedFreeMediationOCON9x();
        Response response = FullDefenceResponse.builder()
            .freeMediation(YES)
            .defenceType(DefenceType.ALREADY_PAID)
            .build();
        Claim claim = SampleClaim.builder()
            .withIssuedPaperFormIssueDate(LocalDate.parse(DATE))
            .withResponse(response).withClaimantResponse(claimantResponse).build();
        ClaimantResponseEvent event = new ClaimantResponseEvent(claim, authorisation);

        ClaimantResponse claimantResponseDisputesAll = ClaimantResponseRejection.validRejectionWithRejectedFreeMediationOCON9x();
        Response responseDisputesAll = FullDefenceResponse.builder()
            .freeMediation(YES)
            .defenceType(DefenceType.DISPUTE)
            .build();
        Claim claimDisputesAll = SampleClaim.builder()
            .withIssuedPaperFormIssueDate(LocalDate.parse(DATE))
            .withResponse(responseDisputesAll).withClaimantResponse(claimantResponseDisputesAll).build();
        ClaimantResponseEvent eventDisputesAll = new ClaimantResponseEvent(claimDisputesAll, authorisation);

        //when
        handler.sendNotificationToDefendant(event);
        handler.sendNotificationToDefendant(eventDisputesAll);
        //then
        verify(claimantRejectionDefendantNotificationService, atLeastOnce())
            .printClaimantMediationRejection(
                eq(claim),
                eq(null),
                eq(authorisation));
    }
}
