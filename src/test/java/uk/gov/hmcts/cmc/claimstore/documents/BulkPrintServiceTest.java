package uk.gov.hmcts.cmc.claimstore.documents;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintableTemplate;
import uk.gov.hmcts.cmc.claimstore.services.staff.BulkPrintStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;
import uk.gov.hmcts.reform.sendletter.api.Letter;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.BULK_PRINT_FAILED;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.ADDITIONAL_DATA_CASE_IDENTIFIER_KEY;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.ADDITIONAL_DATA_LETTER_TYPE_KEY;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.FIRST_CONTACT_LETTER_TYPE;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.XEROX_TYPE_PARAMETER;

@RunWith(MockitoJUnitRunner.class)
public class BulkPrintServiceTest {
    private static final String AUTH_VALUE = "AuthValue";
    private static final Claim CLAIM = SampleClaim.getDefault();
    protected static final byte[] PDF_BYTES = new byte[] {1, 2, 3, 4};
    private static final Map<String, Object> additionalData = new HashMap<>();
    private static final Map<String, Object> pinContents = new HashMap<>();
    private static final Document defendantLetterDocument = new Document("pinTemplate", pinContents);
    private static final Map<String, Object> claimContents = new HashMap<>();
    private static final Document sealedClaimDocument = new Document("sealedClaimTemplate", claimContents);

    @Mock
    private SendLetterApi sendLetterApi;

    @Mock
    private AppInsights appInsights;

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    private BulkPrintService bulkPrintService;
    @Mock
    private BulkPrintStaffNotificationService bulkPrintStaffNotificationService;
    @Mock
    private PDFServiceClient pdfServiceClient;
    private Letter letter;

    @Before
    public void beforeEachTest() {
        when(authTokenGenerator.generate()).thenReturn(AUTH_VALUE);
        additionalData.put(ADDITIONAL_DATA_LETTER_TYPE_KEY, FIRST_CONTACT_LETTER_TYPE);
        additionalData.put(ADDITIONAL_DATA_CASE_IDENTIFIER_KEY, CLAIM.getId());
        additionalData.put(ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY, CLAIM.getReferenceNumber());

        List<Document> documents = Arrays.asList(defendantLetterDocument, sealedClaimDocument);
        letter = new Letter(documents, XEROX_TYPE_PARAMETER, additionalData);
    }

    @Test
    public void shouldSendLetterWithDocumentsAsInGivenOrder() {
        //given
        List<Document> documents = Arrays.asList(defendantLetterDocument, sealedClaimDocument);
        when(sendLetterApi.sendLetter(eq(AUTH_VALUE), eq(new Letter(documents, XEROX_TYPE_PARAMETER, additionalData))))
            .thenReturn(new SendLetterResponse(UUID.randomUUID()));

        bulkPrintService = new BulkPrintService(
            sendLetterApi,
            authTokenGenerator,
            bulkPrintStaffNotificationService,
            appInsights,
            pdfServiceClient
        );

        //when
        bulkPrintService.print(CLAIM,
            ImmutableList.of(
                new PrintableTemplate(defendantLetterDocument, "filename"),
                new PrintableTemplate(sealedClaimDocument, "filename")
            ));
        //then

        verify(sendLetterApi).sendLetter(eq(AUTH_VALUE),
            eq(new Letter(documents, XEROX_TYPE_PARAMETER, additionalData)));
    }

    @Test
    public void shouldSendLetterWithPDFs() {
        //given
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).thenReturn(PDF_BYTES);
        when(sendLetterApi.sendLetter(eq(AUTH_VALUE), any(LetterWithPdfsRequest.class)))
            .thenReturn(new SendLetterResponse(UUID.randomUUID()));

        Document legalOrderDocument = new Document("legalOrder", Collections.emptyMap());
        Map<String, Object> coverContents = new HashMap<>();
        coverContents.put("item", "value");
        Document coversheetForClaimant = new Document("coversheetForClaimant", coverContents);

        bulkPrintService = new BulkPrintService(
            sendLetterApi,
            authTokenGenerator,
            bulkPrintStaffNotificationService,
            appInsights,
            pdfServiceClient
        );
        //when
        bulkPrintService.printPdf(CLAIM, ImmutableList.of(
            new PrintableTemplate(coversheetForClaimant, "filename"),
            new PrintableTemplate(legalOrderDocument, "filename")
        ));

        verify(sendLetterApi).sendLetter(eq(AUTH_VALUE), any(LetterWithPdfsRequest.class));
    }

    @Test(expected = RuntimeException.class)
    public void shouldNotifyStaffOnPrintFailure() {
        //given
        doThrow(new RuntimeException("send Letter failed"))
            .when(sendLetterApi)
            .sendLetter(eq(AUTH_VALUE), eq(letter));

        //when
        bulkPrintService = new BulkPrintService(
            sendLetterApi,
            authTokenGenerator,
            bulkPrintStaffNotificationService,
            appInsights,
            pdfServiceClient
        );
        try {
            bulkPrintService.print(
                CLAIM,
                ImmutableList.of(
                    new PrintableTemplate(defendantLetterDocument, "filename"),
                    new PrintableTemplate(sealedClaimDocument, "filename")
                ));
        } finally {
            //then
            verify(sendLetterApi).sendLetter(eq(AUTH_VALUE), eq(letter));
        }
    }

    @Test(expected = RuntimeException.class)
    public void recoveryThrowWhenAsyncEnabled() {
        //given
        RuntimeException exception = new RuntimeException("send Letter failed");

        //when
        bulkPrintService = new BulkPrintService(
            sendLetterApi,
            authTokenGenerator,
            bulkPrintStaffNotificationService,
            appInsights,
            pdfServiceClient
        );
        ReflectionTestUtils.setField(bulkPrintService,
            "feature_toggles.async_event_operations_enabled",
            true);
        try {
            bulkPrintService
                .notifyStaffForBulkPrintFailure(
                    exception,
                    CLAIM,
                    ImmutableList.of(
                        new PrintableTemplate(defendantLetterDocument, "filename"),
                        new PrintableTemplate(sealedClaimDocument, "filename")
                    ));
        } finally {
            //then
            verify(bulkPrintStaffNotificationService).notifyFailedBulkPrint(
                eq(ImmutableList.of(
                    new PrintableTemplate(defendantLetterDocument, "filename"),
                    new PrintableTemplate(sealedClaimDocument, "filename"))),
                eq(CLAIM)
            );

            verify(appInsights).trackEvent(eq(BULK_PRINT_FAILED), eq(REFERENCE_NUMBER), eq(CLAIM.getReferenceNumber()));
        }
    }
}
