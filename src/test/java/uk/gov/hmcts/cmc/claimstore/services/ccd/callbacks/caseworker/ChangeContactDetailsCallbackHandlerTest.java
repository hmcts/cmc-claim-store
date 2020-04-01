package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactChangeContent;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.GENERAL_LETTER_PDF;

@ExtendWith(MockitoExtension.class)
public class ChangeContactDetailsCallbackHandlerTest {
    @Mock
    private ChangeContactDetailsNotificationService changeContactDetailsNotificationService;

    private ChangeContactDetailsPostProcessor changeContactDetailsPostProcessor;

    private ChangeContactDetailsCallbackHandler handler;

    private CallbackRequest callbackRequest;

    private CallbackParams callbackParams;
    @Mock
    private LetterContentBuilder letterContentBuilder;

    private CaseDetailsConverter caseDetailsConverter;

    private CCDContactChangeContent contactChangeContent;

    private LetterGeneratorService letterGeneratorService;

    private GeneralLetterService generalLetterService;

    private static final String reference = "to-%s-contact-details-changed-%s";
    private static final String NO_DETAILS_CHANGED_ERROR =
        "Notifications cannot be send if contact details were not changed.";
    private static final String AUTHORISATION_TOKEN = "Bearer let me in";
    private static final String DOCUMENT_URL = "http://bla.test";

    private Map<String, Object> data;
    private static final String DOC_URL = "http://success.test";
    private static final String DOC_URL_BINARY = "http://success.test/binary";
    private static final String DOC_NAME = "doc-name";
    private static final String LETTER_CONTENT = "letterContent";
    private static final String CHANGE_CONTACT_PARTY = "changeContactParty";
    private static final CCDDocument DRAFT_LETTER_DOC = CCDDocument.builder()
        .documentFileName(DOC_NAME)
        .documentBinaryUrl(DOC_URL_BINARY)
        .documentUrl(DOC_URL).build();
    private static final URI DOCUMENT_URI = URI.create("http://localhost/doc.pdf");
    private static final Claim claim = SampleClaim
        .builder()
        .build();
    private static final String DOCUMENT_BINARY_URL = "http://bla.binary.test";
    private static final String DOCUMENT_FILE_NAME = "sealed_claim.pdf";
    private static final LocalDateTime DATE = LocalDateTime.parse("2020-11-16T13:15:30");
    private static final byte[] PDF_BYTES = new byte[] {1, 2, 3, 4};
    private static final String ERROR_MESSAGE =
        "There was a technical problem. Nothing has been sent. You need to try again.";
    private static final String DRAFT_LETTER_DOC_KEY = "draftLetterDoc";

    private static final CCDDocument DOCUMENT = CCDDocument
        .builder()
        .documentUrl(DOCUMENT_URL)
        .documentBinaryUrl(DOCUMENT_BINARY_URL)
        .documentFileName(DOCUMENT_FILE_NAME)
        .build();
    private static final CCDCollectionElement<CCDClaimDocument> CLAIM_DOCUMENT =
        CCDCollectionElement.<CCDClaimDocument>builder()
            .value(CCDClaimDocument.builder()
                .documentLink(DOCUMENT)
                .createdDatetime(DATE)
                .documentName("general-letter-2020-01-01")
                .documentType(CCDClaimDocumentType.GENERAL_LETTER)
                .build())
            .build();
    private CCDCase ccdCase;

    private CaseDetails caseDetails;

    private UserDetails userDetails;

    @Mock
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        handler = new ChangeContactDetailsCallbackHandler(changeContactDetailsPostProcessor);
        String documentUrl = DOCUMENT_URI.toString();
        CCDDocument document = new CCDDocument(documentUrl, documentUrl, GENERAL_LETTER_PDF);
        ccdCase = CCDCase.builder()
            .previousServiceCaseReference("000MC001")
            .caseDocuments(ImmutableList.of(CCDCollectionElement.<CCDClaimDocument>builder()
                .value(CCDClaimDocument.builder()
                    .documentLink(document)
                    .documentType(CCDClaimDocumentType.GENERAL_LETTER)
                    .documentName("general-letter")
                    .build())
                .build()))
            .draftLetterDoc(DRAFT_LETTER_DOC).build();
        data = new HashMap<>();
        data.put(CHANGE_CONTACT_PARTY, "claimant");
        data.put(LETTER_CONTENT, "content");
        caseDetails = CaseDetails.builder()
            .data(data)
            .build();
        userDetails = SampleUserDetails.builder()
            .withForename("Judge")
            .withSurname("McJudge")
            .build();
    }

    @Test
    void shouldCompareContactDetails() {
        CCDParty partyA = SampleData.getCCDPartyWithEmail("j@bfdj.com");
        CCDParty partyB = SampleData.getCCDPartyWithEmail("dhej@jhdk.com");
        letterContentBuilder.letterContent(partyA, partyB);
        assertThat(contactChangeContent.getIsEmailModified().getValue()).isEqualTo("yes");
    }

    @Test
    void shouldCreateLetterIfDefendantNotLinked() {
        //set up call back params where
        // defendant is not linked
        // two claimants are different
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase = SampleData.addContactChanges(ccdCase);
        given(changeContactDetailsPostProcessor.letterNeededForDefendant(any(), any()))
            .willReturn(true);
        changeContactDetailsPostProcessor.showNewContactDetails(callbackParams);
        verify(letterGeneratorService).createGeneralLetter(
            eq(ccdCase),
            eq(AUTHORISATION_TOKEN));
    }

    @Test
    public void shouldSendEmailToClaimantUsingPredefinedTemplate() {
        Claim claim = SampleClaim.builder().build();
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase = SampleData.addContactChangePartyDefendant(ccdCase);
        Map<String, String> expectedParams = ImmutableMap.of(
            "claimReferenceNumber", claim.getReferenceNumber(),
            "claimantName", claim.getClaimData().getClaimant().getName(),
            "defendantName", claim.getClaimData().getDefendant().getName(),
            "frontendBaseUrl", FRONTEND_BASE_URL,
            "externalId", claim.getExternalId()
        );
        changeContactDetailsNotificationService.sendEmailToRightRecipient(ccdCase, claim);
        verify(notificationService).sendMail(
            eq(SampleClaim.SUBMITTER_EMAIL),
            eq("claimantContactDetailsChanged"),
            eq(expectedParams),
            eq(String.format(reference, "claimant", claim.getReferenceNumber())));
    }

    @Test
    public void shouldSendEmailToDefendantUsingPredefinedTemplate() {
        Claim claim = SampleClaim.builder().build();
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase = SampleData.addContactChangePartyClaimant(ccdCase);
        Map<String, String> expectedParams = ImmutableMap.of(
            "claimReferenceNumber", claim.getReferenceNumber(),
            "claimantName", claim.getClaimData().getClaimant().getName(),
            "defendantName", claim.getClaimData().getDefendant().getName(),
            "frontendBaseUrl", FRONTEND_BASE_URL,
            "externalId", claim.getExternalId()
        );
        changeContactDetailsNotificationService.sendEmailToRightRecipient(ccdCase, claim);
        verify(notificationService).sendMail(
            eq(SampleClaim.DEFENDANT_EMAIL),
            eq("defendantContactDetailsChanged"),
            eq(expectedParams),
            eq(String.format(reference, "defendant", claim.getReferenceNumber())));
    }

    @Test
    public void shouldPrintAndUpdateCaseDocumentsIfDefendantNotLinked() {
        Claim claim = SampleClaim.builder().build();
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase = SampleData.addContactChangePartyClaimant(ccdCase);
        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();

        given(changeContactDetailsPostProcessor
            .letterNeededForDefendant(ccdCase.getContactChangeParty(), ccdCase)).willReturn(true);
        verify(generalLetterService).printAndUpdateCaseDocuments(caseDetails, authorisation);
    }

    @Test
    public void shouldReturnErrorIfNoContactDetailsWereChanged() {
        //CallbackParams callbackParams = CallbackParams.builder()
        //        .type(CallbackType.ABOUT_TO_SUBMIT)
        //        .request(callbackRequest)
        //        .build();
        //(changeContactDetailsPostProcessor.showNewContactDetails(callbackParams);
        //AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
        //        handler.handle(callbackParams);
        //
        //assertThat(response.getErrors()).containsExactly(NO_DETAILS_CHANGED_ERROR);
    }
}
