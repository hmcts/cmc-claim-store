package uk.gov.hmcts.cmc.claimstore.documents;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.mapper.AddressMapper;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.DefendantPinLetterContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.InterestContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CitizenServiceDocumentsServiceTest {
    protected static final String AUTHORISATION_TOKEN = "Bearer token";
    private static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};
    private static final String PIN = "123";
    private static final String partyName = "Dr. John Smith";
    private static final String claimAmount = "£80.99";
    private static final String refernceNumber = "000MC001";
    private static final String claimantName = "John Rambo";
    private static final LocalDate RESPONSE_DEADLINE = LocalDate.now().plusDays(15);

    private String defendantPinLetterTemplateID;
    private CitizenServiceDocumentsService citizenServiceDocumentsService;
    @Mock
    private DocumentTemplates documentTemplates;
    @Mock
    private ClaimContentProvider claimContentProvider;
    @Mock
    private DefendantPinLetterContentProvider letterContentProvider;
    @Mock
    private DocAssemblyService docAssemblyService;

    @Mock
    private CaseMapper caseMapper;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private StaffEmailProperties staffEmailProperties;
    @Mock
    private InterestContentProvider interestContentProvider;
    @Mock
    private AddressMapper addressMapper;

    @Before
    public void beforeEachTest() {
        defendantPinLetterTemplateID = "XYZ";
        citizenServiceDocumentsService
            = new CitizenServiceDocumentsService(documentTemplates, claimContentProvider, letterContentProvider,
            docAssemblyService, defendantPinLetterTemplateID, caseMapper, notificationsProperties, staffEmailProperties,
            interestContentProvider, addressMapper);
    }

    @Test
    public void shouldReturnSealedClaimDocument() {
        //given
        when(documentTemplates.getSealedClaim()).thenReturn(PDF_BYTES);

        Claim claim = SampleClaim.getDefault();
        Map<String, Object> documentContent = new HashMap<>();
        documentContent.put("referenceNumber", "000MC001");
        when(claimContentProvider.createContent(claim)).thenReturn(documentContent);
        //when
        Document result = citizenServiceDocumentsService.sealedClaimDocument(claim);
        //then
        assertThat(result.template).isEqualTo(new String(PDF_BYTES));

        //verify
        verify(claimContentProvider).createContent(eq(claim));
        verify(documentTemplates).getSealedClaim();
    }

    @Test
    public void shouldReturnPinLetterDocument() {
        //given
        when(documentTemplates.getDefendantPinLetter()).thenReturn(PDF_BYTES);

        Claim claim = SampleClaim.getDefault();
        Map<String, Object> documentContent = new HashMap<>();
        documentContent.put("referenceNumber", "000MC001");
        String defendantPin = "67Khy890";
        when(letterContentProvider.createContent(claim, defendantPin)).thenReturn(documentContent);
        //when
        Document result = citizenServiceDocumentsService.pinLetterDocument(claim, defendantPin);
        //then
        assertThat(result.template).isEqualTo(new String(PDF_BYTES));

        //verify
        verify(letterContentProvider).createContent(eq(claim), eq(defendantPin));
        verify(documentTemplates).getDefendantPinLetter();
    }

    @Test
    public void shouldCreatePinLetter() {
        DocAssemblyTemplateBody docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
        CCDDocument ccdDocument = CCDDocument.builder().build();
        Claim claim = SampleClaim.getDefault();
        CCDCase ccdCase = CCDCase.builder().build();

        when(caseMapper.to(eq(claim))).thenReturn(ccdCase);

        when(docAssemblyService
            .generateDocument(any(CCDCase.class), anyString(), any(DocAssemblyTemplateBody.class), anyString()))
            .thenReturn(ccdDocument);
        citizenServiceDocumentsService.createDefendantPinLetter(claim, PIN, AUTHORISATION_TOKEN);

        verify(docAssemblyService).generateDocument(any(CCDCase.class), eq(AUTHORISATION_TOKEN),
            any(DocAssemblyTemplateBody.class), eq(defendantPinLetterTemplateID));
    }

    @Test
    public void shouldMapTemplateBodyWhenPinLetterForDefendant() {
        Claim claim = SampleClaim.getDefault();
        DocAssemblyTemplateBody requestBody = citizenServiceDocumentsService
            .pinLetterTemplateMapper(claim, PIN);
        DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
            .partyName(partyName)
            .claimAmount(claimAmount)
            .referenceNumber(refernceNumber)
            .partyAddress(null)
            .claimantName(claimantName)
            .responseDeadline(RESPONSE_DEADLINE)
            .staffEmail(null)
            .defendantPin(PIN)
            .respondToClaimUrl(null)
            .build();
        AssertionsForClassTypes.assertThat(requestBody).isEqualTo(expectedBody);
    }
}
