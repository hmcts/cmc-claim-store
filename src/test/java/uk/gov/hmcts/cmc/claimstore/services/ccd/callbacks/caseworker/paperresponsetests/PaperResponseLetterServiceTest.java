package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperresponsetests;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.courtfinder.models.Court;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.PaperDefenceLetterBodyMapper;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.PaperResponseLetterService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.DQ_FLAG;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.LA_PILOT_FLAG;

@ExtendWith(MockitoExtension.class)
class PaperResponseLetterServiceTest {
    protected static final String AUTHORISATION_TOKEN = "Bearer token";
    private static final String OCON_INDIVIDUAL_DQS = "indDqTemplateID";
    private static final String OCON_INDIVIDUAL = "indDqTemplateID";
    private static final String OCON_COMPANY_DQS = "oconCompDqTemplateID";
    private static final String OCON_COMPANY = "oconCompTemplateID";
    private static final String OCON_SOLE_TRADER_DQS = "oconSoleDqTemplateID";
    private static final String OCON_SOLE_TRADER = "oconSoleTemplateID";
    private static final String COVER_LETTER = "coverLetter";
    private static final LocalDate EXTENDED_RESPONSE_DEADLINE = LocalDate.now();
    private static final String HEARING_COURT = "Shoreditch";
    private static final String POST_CODE = "postcode";
    private static final UserDetails CITIZEN_DETAILS = SampleUserDetails.builder()
        .withRoles(Role.CITIZEN.getRole())
        .withUserId(SampleClaim.USER_ID).build();

    private static final String DOC_URL = "http://success.test";
    private static final String DOC_URL_BINARY = "http://success.test/binary";
    private static final String DOC_NAME = "doc-name";
    private static final CCDDocument COVER_LETTER_DOC = CCDDocument
        .builder()
        .documentUrl(DOC_URL)
        .documentBinaryUrl(DOC_URL_BINARY)
        .documentFileName(DOC_NAME)
        .build();

    private PaperResponseLetterService paperResponseLetterService;
    @Mock
    private PaperDefenceLetterBodyMapper paperDefenceLetterBodyMapper;
    @Mock
    private DocAssemblyService docAssemblyService;
    @Mock
    private UserService userService;
    @Mock
    private GeneralLetterService generalLetterService;
    private CCDCase ccdCase;
    private Claim claim;
    private DocAssemblyTemplateBody docAssemblyTemplateBody;
    @Mock
    private CourtFinderApi courtFinderApi;

    @BeforeEach
    void setUp() {
        paperResponseLetterService = new PaperResponseLetterService(
            COVER_LETTER,
            OCON_INDIVIDUAL_DQS,
            OCON_INDIVIDUAL,
            OCON_COMPANY_DQS,
            OCON_COMPANY,
            OCON_SOLE_TRADER_DQS,
            OCON_SOLE_TRADER,
            paperDefenceLetterBodyMapper,
            docAssemblyService,
            userService,
            generalLetterService,
            courtFinderApi
        );

        ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase.setCaseName("case name");
        ccdCase.setApplicants(
            ImmutableList.of(
                CCDCollectionElement.<CCDApplicant>builder()
                    .value(SampleData.getCCDApplicantIndividual())
                    .build()
            ));
        docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
    }

    @Test
    void shouldCreateCoverLetter() {
        DocAssemblyTemplateBody docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
        CCDDocument ccdDocument = CCDDocument.builder().build();

        when(paperDefenceLetterBodyMapper
            .coverLetterTemplateMapper(any(CCDCase.class), anyString(), any(LocalDate.class)))
            .thenReturn(docAssemblyTemplateBody);
        when(docAssemblyService
            .generateDocument(any(CCDCase.class), anyString(), any(DocAssemblyTemplateBody.class), anyString()))
            .thenReturn(ccdDocument);

        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(CITIZEN_DETAILS);

        paperResponseLetterService.createCoverLetter(ccdCase, AUTHORISATION_TOKEN, LocalDate.now());

        verify(paperDefenceLetterBodyMapper).coverLetterTemplateMapper(eq(ccdCase),
            eq(CITIZEN_DETAILS.getFullName()), eq(LocalDate.now()));
        verify(docAssemblyService).generateDocument(any(CCDCase.class), eq(AUTHORISATION_TOKEN),
            eq(docAssemblyTemplateBody), anyString());
    }

    @Nested
    @DisplayName("Tests whose claims have online DQs")
    class WithDQsTests {

        @BeforeEach
        void setUp() {
            claim = SampleClaim.builder()
                .withFeatures(ImmutableList.of(DQ_FLAG.getValue(), LA_PILOT_FLAG.getValue()))
                .withResponseDeadline(LocalDate.now().minusMonths(2))
                .withResponse(SampleResponse.PartAdmission.builder().buildWithDirectionsQuestionnaire())
                .build();
        }

        @Test
        void shouldCreateOconFormForIndividualWithDQs() {
            ccdCase.setRespondents(
                ImmutableList.of(
                    CCDCollectionElement.<CCDRespondent>builder()
                        .value(SampleData.getCCDRespondentIndividual())
                        .build()
                ));
            when(paperDefenceLetterBodyMapper
                .oconFormIndividualWithDQsMapper(any(CCDCase.class), any(LocalDate.class), any(String.class)))
                .thenReturn(docAssemblyTemplateBody);
            when(courtFinderApi.findMoneyClaimCourtByPostcode(eq(POST_CODE)))
                .thenReturn(List.of(Court.builder().name(HEARING_COURT).build()));
            paperResponseLetterService.createOconForm(ccdCase, claim, AUTHORISATION_TOKEN,
                EXTENDED_RESPONSE_DEADLINE);
            verify(paperDefenceLetterBodyMapper)
                .oconFormIndividualWithDQsMapper(eq(ccdCase), eq(EXTENDED_RESPONSE_DEADLINE), eq(HEARING_COURT));
            verify(docAssemblyService).generateDocument(eq(ccdCase), eq(AUTHORISATION_TOKEN),
                eq(docAssemblyTemplateBody), anyString());
        }

        @Test
        void shouldCreateOconFormForSoleTraderWthDQs() {
            ccdCase.setRespondents(
                ImmutableList.of(
                    CCDCollectionElement.<CCDRespondent>builder()
                        .value(SampleData.getCCDRespondentSoleTrader())
                        .build()
                ));
            when(paperDefenceLetterBodyMapper
                .oconFormSoleTraderWithDQsMapper(any(CCDCase.class), any(LocalDate.class), any(String.class)))
                .thenReturn(docAssemblyTemplateBody);
            when(courtFinderApi.findMoneyClaimCourtByPostcode(eq(POST_CODE)))
                .thenReturn(List.of(Court.builder().name(HEARING_COURT).build()));
            paperResponseLetterService.createOconForm(ccdCase, claim, AUTHORISATION_TOKEN,
                EXTENDED_RESPONSE_DEADLINE);
            verify(paperDefenceLetterBodyMapper)
                .oconFormSoleTraderWithDQsMapper(eq(ccdCase), eq(EXTENDED_RESPONSE_DEADLINE), eq(HEARING_COURT));
            verify(docAssemblyService).generateDocument(eq(ccdCase), eq(AUTHORISATION_TOKEN),
                eq(docAssemblyTemplateBody), anyString());
        }

        @Test
        void shouldCreateOconFormForCompanyWithDQs() {
            ccdCase.setRespondents(
                ImmutableList.of(
                    CCDCollectionElement.<CCDRespondent>builder()
                        .value(SampleData.getCCDRespondentCompany())
                        .build()
                ));
            when(paperDefenceLetterBodyMapper
                .oconFormOrganisationWithDQsMapper(any(CCDCase.class), any(LocalDate.class), any(String.class)))
                .thenReturn(docAssemblyTemplateBody);
            when(courtFinderApi.findMoneyClaimCourtByPostcode(eq(POST_CODE)))
                .thenReturn(List.of(Court.builder().name(HEARING_COURT).build()));
            paperResponseLetterService.createOconForm(ccdCase, claim, AUTHORISATION_TOKEN,
                EXTENDED_RESPONSE_DEADLINE);
            verify(paperDefenceLetterBodyMapper)
                .oconFormOrganisationWithDQsMapper(eq(ccdCase), eq(EXTENDED_RESPONSE_DEADLINE), eq(HEARING_COURT));
            verify(docAssemblyService).generateDocument(eq(ccdCase), eq(AUTHORISATION_TOKEN),
                eq(docAssemblyTemplateBody), anyString());
        }

    }

    @Nested
    @DisplayName("Tests whose claims do not have online DQs")
    class WithoutDQsTests {

        @BeforeEach
        void setUp() {
            claim = SampleClaim.builder()
                .build();
        }

        @Test
        void shouldCreateOconFormForIndividualWithoutDQs() {
            ccdCase.setRespondents(
                ImmutableList.of(
                    CCDCollectionElement.<CCDRespondent>builder()
                        .value(SampleData.getCCDRespondentIndividual())
                        .build()
                ));
            when(paperDefenceLetterBodyMapper
                .oconFormIndividualWithoutDQsMapper(any(CCDCase.class), any(LocalDate.class)))
                .thenReturn(docAssemblyTemplateBody);
            when(courtFinderApi.findMoneyClaimCourtByPostcode(eq(POST_CODE)))
                .thenReturn(List.of(Court.builder().name(HEARING_COURT).build()));
            paperResponseLetterService.createOconForm(ccdCase, claim, AUTHORISATION_TOKEN,
                EXTENDED_RESPONSE_DEADLINE);
            verify(paperDefenceLetterBodyMapper)
                .oconFormIndividualWithoutDQsMapper(eq(ccdCase), eq(EXTENDED_RESPONSE_DEADLINE));
            verify(docAssemblyService).generateDocument(eq(ccdCase), eq(AUTHORISATION_TOKEN),
                eq(docAssemblyTemplateBody), anyString());

        }

        @Test
        void shouldCreateOconFormForSoleTraderWithoutDQs() {
            ccdCase.setRespondents(
                ImmutableList.of(
                    CCDCollectionElement.<CCDRespondent>builder()
                        .value(SampleData.getCCDRespondentSoleTrader())
                        .build()
                ));
            when(paperDefenceLetterBodyMapper
                .oconFormSoleTraderWithoutDQsMapper(any(CCDCase.class), any(LocalDate.class)))
                .thenReturn(docAssemblyTemplateBody);
            when(courtFinderApi.findMoneyClaimCourtByPostcode(eq(POST_CODE)))
                .thenReturn(List.of(Court.builder().name(HEARING_COURT).build()));
            paperResponseLetterService.createOconForm(ccdCase, claim, AUTHORISATION_TOKEN,
                EXTENDED_RESPONSE_DEADLINE);
            verify(paperDefenceLetterBodyMapper)
                .oconFormSoleTraderWithoutDQsMapper(eq(ccdCase), eq(EXTENDED_RESPONSE_DEADLINE));
            verify(docAssemblyService).generateDocument(eq(ccdCase), eq(AUTHORISATION_TOKEN),
                eq(docAssemblyTemplateBody), anyString());

        }

        @Test
        void shouldCreateOconFormForCompanyWithoutDQs() {
            docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
            ccdCase.setRespondents(
                ImmutableList.of(
                    CCDCollectionElement.<CCDRespondent>builder()
                        .value(SampleData.getCCDRespondentOrganisation())
                        .build()
                ));
            when(paperDefenceLetterBodyMapper
                .oconFormOrganisationWithoutDQsMapper(any(CCDCase.class), any(LocalDate.class)))
                .thenReturn(docAssemblyTemplateBody);
            when(courtFinderApi.findMoneyClaimCourtByPostcode(eq(POST_CODE)))
                .thenReturn(List.of(Court.builder().name(HEARING_COURT).build()));
            paperResponseLetterService.createOconForm(ccdCase, claim, AUTHORISATION_TOKEN,
                EXTENDED_RESPONSE_DEADLINE);
            verify(paperDefenceLetterBodyMapper)
                .oconFormOrganisationWithoutDQsMapper(eq(ccdCase), eq(EXTENDED_RESPONSE_DEADLINE));
            verify(docAssemblyService).generateDocument(eq(ccdCase), eq(AUTHORISATION_TOKEN),
                eq(docAssemblyTemplateBody), anyString());

        }

        @Test
        void shouldAddCoverLetterToCaseWithDocuments() {
            docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
            ccdCase.setRespondents(
                ImmutableList.of(
                    CCDCollectionElement.<CCDRespondent>builder()
                        .value(SampleData.getCCDRespondentOrganisation())
                        .build()
                ));
            CCDDocument ccdDocument = CCDDocument.builder().build();
            paperResponseLetterService.addCoverLetterToCaseWithDocuments(ccdCase, claim, ccdDocument,
                AUTHORISATION_TOKEN);
            verify(generalLetterService).attachGeneralLetterToCase(any(CCDCase.class), any(CCDDocument.class),
                anyString(), anyString());

        }
    }
}
