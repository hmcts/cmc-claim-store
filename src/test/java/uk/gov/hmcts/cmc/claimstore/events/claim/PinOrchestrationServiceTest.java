package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.PrintService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimIssuedStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@RunWith(MockitoJUnitRunner.class)
public class PinOrchestrationServiceTest {
    public static final Claim CLAIM = SampleClaim.getDefault();
    public static final String AUTHORISATION = "AUTHORISATION";
    public static final String SUBMITTER_NAME = "submitter-name";
    public static final String PIN = "PIN";

    public static final PDF defendantPinLetter = new PDF("0000-pin", "test".getBytes(), DEFENDANT_PIN_LETTER);
    public static final PDF sealedClaim = new PDF("0000-sealed-claim", "test".getBytes(), SEALED_CLAIM);
    public static final String DEFENDANT_EMAIL_TEMPLATE = "Defendant Email Template";

    private Map<String, Object> pinContents = new HashMap<>();
    private String pinTemplate = "pinTemplate";
    private Document defendantPinLetterDocument = new Document(pinTemplate, pinContents);

    private Map<String, Object> claimContents = new HashMap<>();
    private String claimTemplate = "claimTemplate";
    private Document sealedClaimLetterDocument = new Document(claimTemplate, claimContents);

    private PinOrchestrationService pinOrchestrationService;

    @Mock
    private PrintService bulkPrintService;
    @Mock
    private ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService;
    @Mock
    private ClaimIssuedNotificationService claimIssuedNotificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private NotificationTemplates templates;
    @Mock
    private EmailTemplates emailTemplates;
    @Mock
    private ClaimCreationEventsStatusService eventsStatusService;
    @Mock
    private DocumentOrchestrationService documentOrchestrationService;

    private GeneratedDocuments generatedDocuments = GeneratedDocuments.builder()
        .defendantPinLetterDoc(defendantPinLetterDocument)
        .defendantPinLetter(defendantPinLetter)
        .sealedClaimDoc(sealedClaimLetterDocument)
        .sealedClaim(sealedClaim)
        .pin(PIN)
        .build();

    @Before
    public void before() {
        pinOrchestrationService = new PinOrchestrationService(
            bulkPrintService,
            claimIssuedStaffNotificationService,
            claimIssuedNotificationService,
            notificationsProperties,
            eventsStatusService,
            documentOrchestrationService
        );

        given(notificationsProperties.getTemplates()).willReturn(templates);
        given(templates.getEmail()).willReturn(emailTemplates);
        given(emailTemplates.getDefendantClaimIssued()).willReturn(DEFENDANT_EMAIL_TEMPLATE);
    }

    @Test
    public void shouldProcessPinBased() {
        //given
        GeneratedDocuments generatedDocuments = GeneratedDocuments.builder()
            .defendantPinLetterDoc(defendantPinLetterDocument)
            .defendantPinLetter(defendantPinLetter)
            .sealedClaimDoc(sealedClaimLetterDocument)
            .sealedClaim(sealedClaim)
            .pin(PIN)
            .build();

        given(documentOrchestrationService.generateForCitizen(eq(CLAIM), eq(AUTHORISATION)))
            .willReturn(generatedDocuments);

        //when
        pinOrchestrationService.process(CLAIM, AUTHORISATION, SUBMITTER_NAME);

        //then
        verify(bulkPrintService).print(eq(CLAIM), eq(defendantPinLetterDocument), eq(sealedClaimLetterDocument));

        verify(claimIssuedStaffNotificationService)
            .notifyStaffOfClaimIssue(eq(CLAIM), eq(ImmutableList.of(sealedClaim, defendantPinLetter)));

        verify(claimIssuedNotificationService).sendMail(
            eq(CLAIM),
            eq(CLAIM.getClaimData().getDefendant().getEmail().orElse(null)),
            eq(PIN),
            eq(DEFENDANT_EMAIL_TEMPLATE),
            eq("defendant-issue-notification-" + CLAIM.getReferenceNumber()),
            eq(SUBMITTER_NAME)
        );

        verify(eventsStatusService).updateClaimOperationCompletion(eq(AUTHORISATION), eq(CLAIM),
            eq(CaseEvent.PIN_GENERATION_OPERATIONS));
    }

    @Test(expected = RuntimeException.class)
    public void updatePinOperationStatusWhenBulkPrintFails() {
        //given
        given(documentOrchestrationService.generateForCitizen(eq(CLAIM), eq(AUTHORISATION)))
            .willReturn(generatedDocuments);

        doThrow(new RuntimeException("bulk print failed")).when(bulkPrintService).print(any(), any(), any());

        //when
        pinOrchestrationService.process(CLAIM, AUTHORISATION, SUBMITTER_NAME);

        //then
        verify(eventsStatusService).updateClaimOperationCompletion(eq(AUTHORISATION), eq(CLAIM),
            eq(CaseEvent.PIN_GENERATION_OPERATIONS));
    }

    @Test(expected = RuntimeException.class)
    public void updatePinOperationStatusWhenClaimIssueNotificationFails() {
        //given
        given(documentOrchestrationService.generateForCitizen(eq(CLAIM), eq(AUTHORISATION)))
            .willReturn(generatedDocuments);
        doThrow(new RuntimeException("claim issue notification failed"))
            .when(claimIssuedStaffNotificationService).notifyStaffOfClaimIssue(any(), any());
        //when
        pinOrchestrationService.process(CLAIM, AUTHORISATION, SUBMITTER_NAME);

        //then
        verify(eventsStatusService).updateClaimOperationCompletion(eq(AUTHORISATION), eq(CLAIM),
            eq(CaseEvent.PIN_GENERATION_OPERATIONS));
    }

    @Test(expected = RuntimeException.class)
    public void updatePinOperationStatusWhenNoifyDefendantFails() {
        //given
        given(documentOrchestrationService.generateForCitizen(eq(CLAIM), eq(AUTHORISATION)))
            .willReturn(generatedDocuments);
        doThrow(new RuntimeException("claim issue notification failed"))
            .when(claimIssuedNotificationService).sendMail(any(), any(), any(), any(), any(), any());
        //when
        pinOrchestrationService.process(CLAIM, AUTHORISATION, SUBMITTER_NAME);

        //then
        verify(eventsStatusService).updateClaimOperationCompletion(eq(AUTHORISATION), eq(CLAIM),
            eq(CaseEvent.PIN_GENERATION_OPERATIONS));
    }
}
