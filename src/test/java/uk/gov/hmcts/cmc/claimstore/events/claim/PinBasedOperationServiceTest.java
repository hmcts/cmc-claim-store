package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.PrintService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentUploadHandler;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimIssuedStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@RunWith(MockitoJUnitRunner.class)
public class PinBasedOperationServiceTest {
    public static final Claim CLAIM = SampleClaim.getDefault();
    public static final String AUTHORISATION = "AUTHORISATION";
    public static final String SUBMITTER_NAME = "submitter-name";
    public static final String PIN = "PIN";

    public static final PDF pinLetterClaim = new PDF("0000-pin", "test".getBytes(), DEFENDANT_PIN_LETTER);
    public static final PDF sealedClaim = new PDF("0000-sealed-claim", "test".getBytes(), SEALED_CLAIM);
    public static final String DEFENDANT_EMAIL_TEMPLATE = "Defendant Email Template";

    private Map<String, Object> pinContents = new HashMap<>();
    private String pinTemplate = "pinTemplate";
    private Document defendantLetterDocument = new Document(pinTemplate, pinContents);

    private Map<String, Object> claimContents = new HashMap<>();
    private String claimTemplate = "claimTemplate";
    private Document sealedClaimLetterDocument = new Document(claimTemplate, claimContents);

    private PinBasedOperationService pinBasedOperationService;

    @Mock
    private DocumentUploadHandler documentUploadHandler;
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

    @Before
    public void before() {
        pinBasedOperationService = new PinBasedOperationService(
            documentUploadHandler,
            bulkPrintService,
            claimIssuedStaffNotificationService,
            claimIssuedNotificationService,
            notificationsProperties
        );

        given(documentUploadHandler
            .uploadToDocumentManagement(eq(CLAIM), eq(AUTHORISATION), eq(singletonList(pinLetterClaim))))
            .willReturn(CLAIM);

        given(notificationsProperties.getTemplates()).willReturn(templates);
        given(templates.getEmail()).willReturn(emailTemplates);
        given(emailTemplates.getDefendantClaimIssued()).willReturn(DEFENDANT_EMAIL_TEMPLATE);
    }

    @Test
    public void shouldProcessPinBased() {
        //given
        GeneratedDocuments generatedDocuments = GeneratedDocuments.builder()
            .defendantLetterDoc(defendantLetterDocument)
            .defendantLetter(pinLetterClaim)
            .sealedClaimDoc(sealedClaimLetterDocument)
            .sealedClaim(sealedClaim)
            .pin(PIN)
            .build();

        //when
        pinBasedOperationService.process(CLAIM, AUTHORISATION, SUBMITTER_NAME, generatedDocuments);

        //then
        verify(documentUploadHandler)
            .uploadToDocumentManagement(eq(CLAIM), eq(AUTHORISATION), eq(singletonList(pinLetterClaim)));

        verify(bulkPrintService).print(eq(CLAIM), eq(defendantLetterDocument), eq(sealedClaimLetterDocument));

        verify(claimIssuedStaffNotificationService)
            .notifyStaffOfClaimIssue(eq(CLAIM), eq(ImmutableList.of(sealedClaim, pinLetterClaim)));

        verify(claimIssuedNotificationService).sendMail(
            eq(CLAIM),
            eq(CLAIM.getClaimData().getDefendant().getEmail().orElse(null)),
            eq(PIN),
            eq(DEFENDANT_EMAIL_TEMPLATE),
            eq("defendant-issue-notification-" + CLAIM.getReferenceNumber()),
            eq(SUBMITTER_NAME)
        );
    }
}
