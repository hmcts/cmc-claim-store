package uk.gov.hmcts.cmc.claimstore.events.claim;

import com.launchdarkly.sdk.LDUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.PrintService;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.Printable;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintableTemplate;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimIssuedStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ADD_BULK_PRINT_DETAILS;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintRequestType.FIRST_CONTACT_LETTER_TYPE;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.domain.models.bulkprint.PrintRequestType.PIN_LETTER_TO_DEFENDANT;

@ExtendWith(MockitoExtension.class)
public class PinOrchestrationServiceTest {
    private static final Claim CLAIM = SampleClaim.getDefault();
    private static final String AUTHORISATION = "AUTHORISATION";
    private static final String SUBMITTER_NAME = "submitter-name";
    private static final String PIN = "PIN";
    private static final List<String> USER_LIST = List.of("Dr. John Smith");

    private static final PDF defendantPinLetter = new PDF("0000-pin", "test".getBytes(), DEFENDANT_PIN_LETTER);
    private static final PDF sealedClaim = new PDF("0000-sealed-claim", "test".getBytes(), SEALED_CLAIM);
    private static final String DEFENDANT_EMAIL_TEMPLATE = "Defendant Email PrintableTemplate";

    private final Map<String, Object> pinContents = new HashMap<>();
    private final String pinTemplate = "pinTemplate";
    private final Document defendantPinLetterDocument = new Document(pinTemplate, pinContents);

    private final Map<String, Object> claimContents = new HashMap<>();
    private final String claimTemplate = "claimTemplate";
    private final Document sealedClaimLetterDocument = new Document(claimTemplate, claimContents);
    private final ImmutableList<Printable> printAbles = ImmutableList.of(
        new PrintableTemplate(
            defendantPinLetterDocument,
            CLAIM.getReferenceNumber() + "-defendant-pin-letter"),
        new PrintableTemplate(
            sealedClaimLetterDocument,
            CLAIM.getReferenceNumber() + "-claim-form")
    );
    private final GeneratedDocuments generatedDocuments = GeneratedDocuments.builder()
        .defendantPinLetterDoc(defendantPinLetterDocument)
        .defendantPinLetter(defendantPinLetter)
        .sealedClaimDoc(sealedClaimLetterDocument)
        .sealedClaim(sealedClaim)
        .pin(PIN)
        .claim(CLAIM)
        .build();
    private final BulkPrintDetails bulkPrintDetails = BulkPrintDetails.builder()
        .printRequestType(PIN_LETTER_TO_DEFENDANT).printRequestId("requestId").build();
    private final ImmutableList<BulkPrintDetails> printDetails = ImmutableList.<BulkPrintDetails>builder()
        .addAll(CLAIM.getBulkPrintDetails())
        .add(bulkPrintDetails)
        .build();
    private final Claim claimWithBulkPrintDetails
        = CLAIM.toBuilder().bulkPrintDetails(List.of(bulkPrintDetails)).build();
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
    @Mock
    private ClaimService claimService;
    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @BeforeEach
    public void before() {
        pinOrchestrationService = new PinOrchestrationService(
            bulkPrintService,
            claimIssuedStaffNotificationService,
            claimIssuedNotificationService,
            notificationsProperties,
            eventsStatusService,
            documentOrchestrationService,
            claimService,
            launchDarklyClient);

        lenient().when(notificationsProperties.getTemplates()).thenReturn(templates);
        lenient().when(templates.getEmail()).thenReturn(emailTemplates);
        lenient().when(emailTemplates.getDefendantClaimIssued()).thenReturn(DEFENDANT_EMAIL_TEMPLATE);

        lenient().when(bulkPrintService
            .printPdf(eq(CLAIM), eq(printAbles), eq(FIRST_CONTACT_LETTER_TYPE), eq(AUTHORISATION), eq(USER_LIST)))
            .thenReturn(bulkPrintDetails);
        lenient().when(bulkPrintService
            .printHtmlLetter(eq(CLAIM), eq(printAbles), eq(FIRST_CONTACT_LETTER_TYPE), eq(AUTHORISATION), eq(USER_LIST)))
            .thenReturn(bulkPrintDetails);
        lenient().when(claimService.addBulkPrintDetails(eq(AUTHORISATION), eq(printDetails),
            eq(ADD_BULK_PRINT_DETAILS), eq(CLAIM)))
            .thenReturn(claimWithBulkPrintDetails);
    }

    @Test
    public void shouldProcessNewPinBased() {
        //given
        given(documentOrchestrationService.generateForCitizen(eq(CLAIM), eq(AUTHORISATION), eq(true)))
            .willReturn(generatedDocuments);
        given(launchDarklyClient.isFeatureEnabled(eq("new-defendant-pin-letter"), any(LDUser.class))).willReturn(true);

        //when
        pinOrchestrationService.process(CLAIM, AUTHORISATION, SUBMITTER_NAME);

        //then
        verify(bulkPrintService).printPdf(
            eq(CLAIM),
            eq(printAbles),
            eq(FIRST_CONTACT_LETTER_TYPE),
            eq(AUTHORISATION),
            eq(USER_LIST));

        verify(claimIssuedStaffNotificationService).notifyStaffOfClaimIssue(eq(claimWithBulkPrintDetails),
            eq(ImmutableList.of(sealedClaim, defendantPinLetter)));

        verify(claimIssuedNotificationService).sendMail(
            eq(claimWithBulkPrintDetails),
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

    @Test
    public void updatePinOperationStatusWhenBulkPrintFailsForNewPinLetter() {
        assertThrows(RuntimeException.class, () -> {
            //given
            given(documentOrchestrationService.generateForCitizen(eq(CLAIM), eq(AUTHORISATION), true))
                .willReturn(generatedDocuments);

            doThrow(new RuntimeException("bulk print failed")).when(bulkPrintService).printPdf(
                any(), anyList(), eq(FIRST_CONTACT_LETTER_TYPE), anyString(), USER_LIST);

            //when
            try {
                pinOrchestrationService.process(CLAIM, AUTHORISATION, SUBMITTER_NAME);

            } finally {
                //then
                verify(eventsStatusService).updateClaimOperationCompletion(eq(AUTHORISATION), eq(CLAIM.getId()),
                    eq(ClaimSubmissionOperationIndicators.builder().build()), eq(CaseEvent.PIN_GENERATION_OPERATIONS));
            }
        });
    }

    @Test
    public void updatePinOperationStatusWhenClaimIssueNotificationFails() {
        assertThrows(RuntimeException.class, () -> {
            //given
            given(documentOrchestrationService.generateForCitizen(eq(CLAIM), eq(AUTHORISATION), true))
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
        });
    }

    @Test
    public void updatePinOperationStatusWhenNotifyDefendantFails() {
        assertThrows(RuntimeException.class, () -> {
            //given
            given(documentOrchestrationService.generateForCitizen(eq(CLAIM), eq(AUTHORISATION), true))
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
        });
    }

    @Test
    public void shouldProcessPinBased() {
        //given
        given(documentOrchestrationService.generateForCitizen(eq(CLAIM), eq(AUTHORISATION), eq(false)))
            .willReturn(generatedDocuments);
        given(launchDarklyClient.isFeatureEnabled(eq("new-defendant-pin-letter"), any(LDUser.class))).willReturn(false);

        //when
        pinOrchestrationService.process(CLAIM, AUTHORISATION, SUBMITTER_NAME);

        //then
        verify(bulkPrintService).printHtmlLetter(
            eq(CLAIM),
            eq(printAbles),
            eq(FIRST_CONTACT_LETTER_TYPE),
            eq(AUTHORISATION),
            eq(USER_LIST));

        verify(claimIssuedStaffNotificationService).notifyStaffOfClaimIssue(eq(claimWithBulkPrintDetails),
            eq(ImmutableList.of(sealedClaim, defendantPinLetter)));

        verify(claimIssuedNotificationService).sendMail(
            eq(claimWithBulkPrintDetails),
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

    @Test
    public void updatePinOperationStatusWhenBulkPrintFails() {
        assertThrows(RuntimeException.class, () -> {
            //given
            given(documentOrchestrationService.generateForCitizen(eq(CLAIM), eq(AUTHORISATION), false))
                .willReturn(generatedDocuments);
            given(launchDarklyClient.isFeatureEnabled(eq("new-defendant-pin-letter"), any(LDUser.class))).willReturn(false);

            doThrow(new RuntimeException("bulk print failed")).when(bulkPrintService).printHtmlLetter(
                any(), anyList(), eq(FIRST_CONTACT_LETTER_TYPE), anyString(), USER_LIST);

            //when
            try {
                pinOrchestrationService.process(CLAIM, AUTHORISATION, SUBMITTER_NAME);

            } finally {
                //then
                verify(eventsStatusService).updateClaimOperationCompletion(eq(AUTHORISATION), eq(CLAIM.getId()),
                    eq(ClaimSubmissionOperationIndicators.builder().build()), eq(CaseEvent.PIN_GENERATION_OPERATIONS));
            }
        });
    }
}
