package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.breathingspace;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.BreathingSpaceEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.DocumentOrchestrationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.RpaOperationService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.breathingspace.BreathingSpaceEmailService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.breathingspace.BreathingSpaceEntetedOrchestrationHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.breathingspace.BreathingSpaceLetterService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.GENERAL_LETTER_PDF;

@RunWith(MockitoJUnitRunner.class)
public class BreathingSpaceEnteredOrchestrationTest {

    public static final String DEFENDANT_EMAIL_TEMPLATE = "Defendant Email Template";
    public static final String CLAIMANT_EMAIL_TEMPLATE = "Claimant Email Template";
    public static final String BREATHING_SPACE_LETTER_TEMPLATE_ID = "breathingSpaceEnteredTemplateID";
    private static final String AUTHORISATION = "Bearer: aaaa";
    private static final String DOC_URL = "http://success.test";
    private static final String DOC_URL_BINARY = "http://success.test/binary";
    private static final String DOC_NAME = "doc-name";
    private static final CCDDocument DRAFT_LETTER_DOC = CCDDocument.builder()
        .documentFileName(DOC_NAME)
        .documentBinaryUrl(DOC_URL_BINARY)
        .documentUrl(DOC_URL).build();
    private static final URI DOCUMENT_URI = URI.create("http://localhost/doc.pdf");
    private CCDCase ccdCase;
    private BreathingSpaceEntetedOrchestrationHandler handler;
    @Mock
    private BreathingSpaceLetterService breathingSpaceLetterService;
    @Mock
    private BreathingSpaceEmailService breathingSpaceEmailService;
    @Mock
    private AppInsights appInsights;

    @Mock
    private DocumentOrchestrationService documentOrchestrationService;
    @Mock
    private RpaOperationService rpaOperationService;

    @Before
    public void setUp() {
        handler = new BreathingSpaceEntetedOrchestrationHandler(breathingSpaceLetterService,
            breathingSpaceEmailService, documentOrchestrationService, rpaOperationService, appInsights);
        String documentUrl = DOCUMENT_URI.toString();
        CCDDocument document = new CCDDocument(documentUrl, documentUrl, GENERAL_LETTER_PDF);
        ccdCase = CCDCase.builder()
            .previousServiceCaseReference("000MC001")
            .caseDocuments(ImmutableList.of(CCDCollectionElement.<CCDClaimDocument>builder()
                .value(CCDClaimDocument.builder()
                    .documentLink(document)
                    .documentType(CCDClaimDocumentType.GENERAL_LETTER)
                    .documentName("000MC001-breathing-space-entered.pdf")
                    .build())
                .build()))
            .draftLetterDoc(DRAFT_LETTER_DOC).build();
    }

    @Test
    public void shouldSendEmailToClaimantUsingPredefinedTemplate() {
        Claim claim = SampleClaim.builder().withSubmitterEmail("claimant@mail.com").build();
        BreathingSpaceEvent event = new BreathingSpaceEvent(claim, ccdCase, AUTHORISATION,
            BREATHING_SPACE_LETTER_TEMPLATE_ID, CLAIMANT_EMAIL_TEMPLATE, DEFENDANT_EMAIL_TEMPLATE, true);
        handler.caseworkerBreathingSpaceEnteredEvent(event);

        verify(breathingSpaceEmailService).sendNotificationToClaimant(
            any(Claim.class),
            anyString());

    }

    @Test
    public void shouldSendEmailToDefendantUsingPredefinedTemplate() {
        Claim claim = SampleClaim.builder().withDefendantEmail("defendant@mail.com").build();
        BreathingSpaceEvent event = new BreathingSpaceEvent(claim, ccdCase, AUTHORISATION,
            BREATHING_SPACE_LETTER_TEMPLATE_ID, CLAIMANT_EMAIL_TEMPLATE, DEFENDANT_EMAIL_TEMPLATE, true);
        handler.caseworkerBreathingSpaceEnteredEvent(event);

        verify(breathingSpaceEmailService).sendEmailNotificationToDefendant(
            any(Claim.class),
            anyString());
    }

    @Test
    public void shouldSendLetterToDefendantUsingPredefinedTemplate() {
        Claim claim = Claim.builder()
            .referenceNumber("000MC001")
            .build();
        BreathingSpaceEvent event = new BreathingSpaceEvent(claim, ccdCase, AUTHORISATION,
            BREATHING_SPACE_LETTER_TEMPLATE_ID, CLAIMANT_EMAIL_TEMPLATE, DEFENDANT_EMAIL_TEMPLATE, true);

        handler.caseworkerBreathingSpaceEnteredEvent(event);

        verify(breathingSpaceLetterService).sendLetterToDefendant(
            any(CCDCase.class),
            any(Claim.class),
            anyString(),
            anyString());
    }

    @Test
    public void shouldNotGenerateLetterAndSendEmail() {
        Claim claim = Claim.builder()
            .referenceNumber("000MC001")
            .build();
        BreathingSpaceEvent event = new BreathingSpaceEvent(claim, ccdCase, AUTHORISATION,
            BREATHING_SPACE_LETTER_TEMPLATE_ID, CLAIMANT_EMAIL_TEMPLATE, DEFENDANT_EMAIL_TEMPLATE, false);

        handler.caseworkerBreathingSpaceEnteredEvent(event);

        verify(breathingSpaceEmailService, times(0)).sendEmailNotificationToDefendant(
            any(Claim.class),
            anyString());

        verify(breathingSpaceLetterService, times(0)).sendLetterToDefendant(
            any(CCDCase.class),
            any(Claim.class),
            anyString(),
            anyString());

        verify(breathingSpaceEmailService, times(0)).sendEmailNotificationToDefendant(
            any(Claim.class),
            anyString());
    }

    @Test
    public void shouldAddTracking() {
        Claim claim = Claim.builder()
            .referenceNumber("000MC001")
            .build();
        BreathingSpaceEvent event = new BreathingSpaceEvent(claim, ccdCase, AUTHORISATION,
            BREATHING_SPACE_LETTER_TEMPLATE_ID, CLAIMANT_EMAIL_TEMPLATE, DEFENDANT_EMAIL_TEMPLATE, true);

        handler.caseworkerBreathingSpaceEnteredEvent(event);

        verify(appInsights).trackEvent(
            AppInsightsEvent.BREATHING_SPACE_ENTERED,
            AppInsights.REFERENCE_NUMBER,
            SampleClaim.REFERENCE_NUMBER);
    }

}
