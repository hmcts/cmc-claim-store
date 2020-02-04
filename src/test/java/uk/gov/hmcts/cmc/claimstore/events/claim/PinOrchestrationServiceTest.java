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
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintableTemplate;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimIssuedStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@RunWith(MockitoJUnitRunner.class)
public class PinOrchestrationServiceTest {
    private static final Claim CLAIM = SampleClaim.getDefault();
    private static final String AUTHORISATION = "AUTHORISATION";
    private static final String SUBMITTER_NAME = "submitter-name";
    private static final String PIN = "PIN";

    private static final PDF defendantPinLetter = new PDF("0000-pin", "test".getBytes(), DEFENDANT_PIN_LETTER);
    private static final PDF sealedClaim = new PDF("0000-sealed-claim", "test".getBytes(), SEALED_CLAIM);
    private static final String DEFENDANT_EMAIL_TEMPLATE = "Defendant Email PrintableTemplate";

    private final Map<String, Object> pinContents = new HashMap<>();
    private final String pinTemplate = "pinTemplate";
    private final Document defendantPinLetterDocument = new Document(pinTemplate, pinContents);

    private final Map<String, Object> claimContents = new HashMap<>();
    private final String claimTemplate = "claimTemplate";
    private final Document sealedClaimLetterDocument = new Document(claimTemplate, claimContents);

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

    private final GeneratedDocuments generatedDocuments = GeneratedDocuments.builder()
        .defendantPinLetterDoc(defendantPinLetterDocument)
        .defendantPinLetter(defendantPinLetter)
        .sealedClaimDoc(sealedClaimLetterDocument)
        .sealedClaim(sealedClaim)
        .pin(PIN)
        .claim(CLAIM)
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
        given(documentOrchestrationService.generateForCitizen(eq(CLAIM), eq(AUTHORISATION)))
            .willReturn(generatedDocuments);

        //when
        pinOrchestrationService.process(CLAIM, AUTHORISATION, SUBMITTER_NAME);

        //then
        verify(bulkPrintService).print(
            eq(CLAIM),
            eq(ImmutableList.of(
                new PrintableTemplate(
                    defendantPinLetterDocument,
                    CLAIM.getReferenceNumber() + "-defendant-pin-letter"),
                new PrintableTemplate(
                    sealedClaimLetterDocument,
                    CLAIM.getReferenceNumber() + "-claim-form")
            )));

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

        ClaimSubmissionOperationIndicators operationIndicators = ClaimSubmissionOperationIndicators.builder()
            .bulkPrint(YesNoOption.YES)
            .staffNotification(YesNoOption.YES)
            .defendantNotification(YesNoOption.YES)
            .build();

        verify(eventsStatusService).updateClaimOperationCompletion(eq(AUTHORISATION), eq(CLAIM.getId()),
            eq(operationIndicators), eq(CaseEvent.PIN_GENERATION_OPERATIONS));
    }

    @Test(expected = RuntimeException.class)
    public void updatePinOperationStatusWhenBulkPrintFails() {
        //given
        given(documentOrchestrationService.generateForCitizen(eq(CLAIM), eq(AUTHORISATION)))
            .willReturn(generatedDocuments);

        doThrow(new RuntimeException("bulk print failed")).when(bulkPrintService).print(
            any(), anyList());

        //when
        try {
            pinOrchestrationService.process(CLAIM, AUTHORISATION, SUBMITTER_NAME);

        } finally {
            //then
            verify(eventsStatusService).updateClaimOperationCompletion(eq(AUTHORISATION), eq(CLAIM.getId()),
                eq(ClaimSubmissionOperationIndicators.builder().build()), eq(CaseEvent.PIN_GENERATION_OPERATIONS));
        }
    }

    @Test(expected = RuntimeException.class)
    public void updatePinOperationStatusWhenClaimIssueNotificationFails() {
        //given
        given(documentOrchestrationService.generateForCitizen(eq(CLAIM), eq(AUTHORISATION)))
            .willReturn(generatedDocuments);
        doThrow(new RuntimeException("claim issue notification failed"))
            .when(claimIssuedStaffNotificationService).notifyStaffOfClaimIssue(any(), any());
        //when
        try {
            pinOrchestrationService.process(CLAIM, AUTHORISATION, SUBMITTER_NAME);
        } finally {
            //then
            ClaimSubmissionOperationIndicators operationIndicators = ClaimSubmissionOperationIndicators.builder()
                .bulkPrint(YesNoOption.YES)
                .build();

            verify(eventsStatusService).updateClaimOperationCompletion(eq(AUTHORISATION), eq(CLAIM.getId()),
                eq(operationIndicators), eq(CaseEvent.PIN_GENERATION_OPERATIONS));
        }
    }

    @Test(expected = RuntimeException.class)
    public void updatePinOperationStatusWhenNotifyDefendantFails() {
        //given
        given(documentOrchestrationService.generateForCitizen(eq(CLAIM), eq(AUTHORISATION)))
            .willReturn(generatedDocuments);
        doThrow(new RuntimeException("claim issue notification failed"))
            .when(claimIssuedNotificationService).sendMail(any(), any(), any(), any(), any(), any());
        //when
        try {
            pinOrchestrationService.process(CLAIM, AUTHORISATION, SUBMITTER_NAME);
        } finally {
            //then
            ClaimSubmissionOperationIndicators operationIndicators = ClaimSubmissionOperationIndicators.builder()
                .bulkPrint(YesNoOption.YES)
                .staffNotification(YesNoOption.YES)
                .build();

            verify(eventsStatusService).updateClaimOperationCompletion(eq(AUTHORISATION), eq(CLAIM.getId()),
                eq(operationIndicators), eq(CaseEvent.PIN_GENERATION_OPERATIONS));
        }
    }
}
