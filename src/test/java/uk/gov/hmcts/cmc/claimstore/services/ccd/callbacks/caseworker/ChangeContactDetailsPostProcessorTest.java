package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

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
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
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

@ExtendWith(MockitoExtension.class)
public class ChangeContactDetailsPostProcessorTest {

        private ChangeContactDetailsPostProcessor changeContactDetailsPostProcessor;

        private CallbackRequest callbackRequest;

        private CallbackParams callbackParams;

        private CaseDetails caseDetails;

        private ChangeContactDetailsCallbackHandler handler;

        private CCDContactChangeContent contactChangeContent;
        @Mock
        private LetterContentBuilder letterContentBuilder;
        @Mock
        private CaseDetailsConverter caseDetailsConverter;
        @Mock
        private UserService userService;
        @Mock
        private ChangeContactDetailsNotificationService changeContactDetailsNotificationService;

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

        @BeforeEach
        void setUp() {
            changeContactDetailsPostProcessor = new ChangeContactDetailsPostProcessor(
                    caseDetailsConverter,
                    letterGeneratorService,
                    changeContactDetailsNotificationService,
                    letterContentBuilder,
                    userService,
                    generalLetterService);

        data = new HashMap<>();
        CCDContactChangeContent contactChangeContent = CCDContactChangeContent.builder()
                .isEmailModified(CCDYesNoOption.YES)
                .build();
        data.put("contactChangeContent", contactChangeContent);
        data.put(CHANGE_CONTACT_PARTY, "claimant");
        data.put(LETTER_CONTENT, "content");
        caseDetails = CaseDetails.builder()
                .data(data)
                .build();
        callbackRequest = CallbackRequest
                .builder()
                .caseDetails(CaseDetails.builder()
                        .data(data)
                        .build())
                .build();
        callbackParams = CallbackParams.builder()
                .request(callbackRequest)
                .params(ImmutableMap.of(BEARER_TOKEN, BEARER_TOKEN))
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
        public void shouldPrintAndUpdateCaseDocumentsIfDefendantNotLinked() {
            CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
            CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
            ccdCase = SampleData.addContactChangePartyClaimant(ccdCase);
            String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();

            given(changeContactDetailsPostProcessor
                    .letterNeededForDefendant(ccdCase.getContactChangeParty(), ccdCase)).willReturn(true);
            verify(generalLetterService).printAndUpdateCaseDocuments(eq(caseDetails), eq(authorisation));
        }

        @Test
        public void shouldReturnErrorIfNoContactDetailsWereChanged() {
            //this probably should be in set up might not work for all test cases
            data = new HashMap<>();
            CCDContactChangeContent contactChangeContent = CCDContactChangeContent.builder()
                    .isEmailModified(CCDYesNoOption.YES)
                    .build();
            data.put("contactChangeContent", contactChangeContent);
            data.put(CHANGE_CONTACT_PARTY, "claimant");
            data.put(LETTER_CONTENT, "content");
            caseDetails = CaseDetails.builder()
                    .data(data)
                    .build();
            callbackRequest = CallbackRequest
                    .builder()
                    .caseDetails(CaseDetails.builder()
                            .data(data)
                            .build())
                    .build();
            callbackParams = CallbackParams.builder()
                    .request(callbackRequest)
                    .params(ImmutableMap.of(BEARER_TOKEN, BEARER_TOKEN))
                    .build();
            changeContactDetailsPostProcessor.showNewContactDetails(callbackParams);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
                    handler.handle(callbackParams);

            assertThat(response.getErrors()).containsExactly(NO_DETAILS_CHANGED_ERROR);
        }

}
