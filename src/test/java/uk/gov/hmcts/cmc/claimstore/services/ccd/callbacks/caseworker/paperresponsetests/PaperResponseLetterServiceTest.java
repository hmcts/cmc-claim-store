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
import uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.postcode.SearchCourtByPostcodeResponse;
import uk.gov.hmcts.cmc.claimstore.models.factapi.courtfinder.search.slug.SearchCourtBySlugResponse;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserDetails;
import uk.gov.hmcts.cmc.claimstore.requests.courtfinder.CourtFinderApi;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.PaperDefenceLetterBodyMapper;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.PaperResponseLetterService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.test.utils.DataFactory;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.DQ_FLAG;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.LA_PILOT_FLAG;

@ExtendWith(MockitoExtension.class)
class PaperResponseLetterServiceTest {

    private static final String SEARCH_BY_SLUG_NEWCASTLE_RESPONSE = "factapi/courtfinder/search/response/slug/SEARCH_BY_SLUG_NEWCASTLE.json";
    private static final String SEARCH_BY_POSTCODE_NEWCASTLE_RESPONSE = "factapi/courtfinder/search/response/postcode/SEARCH_BY_POSTCODE_NEWCASTLE.json";
    protected static final String AUTHORISATION_TOKEN = "Bearer token";
    private static final String OCON_INDIVIDUAL_DQS = "indDqTemplateID";
    private static final String OCON_INDIVIDUAL = "indDqTemplateID";
    private static final String OCON_COMPANY_DQS = "oconCompDqTemplateID";
    private static final String OCON_COMPANY = "oconCompTemplateID";
    private static final String OCON_SOLE_TRADER_DQS = "oconSoleDqTemplateID";
    private static final String OCON_SOLE_TRADER = "oconSoleTemplateID";
    private static final String COVER_LETTER = "coverLetter";
    private static final String OCON9_LETTER = "ocon9Letter";
    private static final String CASE_TYPE_ID = "MoneyClaimCase";
    private static final String JURISDICTION_ID = "CMC";
    private static final LocalDate EXTENDED_RESPONSE_DEADLINE = LocalDate.now();
    private static final boolean DISABLEN9FORM = true;
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
            OCON9_LETTER,
            CASE_TYPE_ID,
            JURISDICTION_ID,
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
            .generateDocument(any(CCDCase.class), anyString(), any(DocAssemblyTemplateBody.class), anyString(), anyString(), anyString()))
            .thenReturn(ccdDocument);

        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(CITIZEN_DETAILS);

        paperResponseLetterService.createCoverLetter(ccdCase, AUTHORISATION_TOKEN, LocalDate.now());

        verify(paperDefenceLetterBodyMapper).coverLetterTemplateMapper(eq(ccdCase),
            eq(CITIZEN_DETAILS.getFullName()), eq(LocalDate.now()));
        verify(docAssemblyService).generateDocument(any(CCDCase.class), eq(AUTHORISATION_TOKEN),
            eq(docAssemblyTemplateBody), anyString(), anyString(), anyString());
    }

    @Test
    void shouldOCON9Letter() {
        DocAssemblyTemplateBody docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
        CCDDocument ccdDocument = CCDDocument.builder().build();

        when(paperDefenceLetterBodyMapper
            .coverLetterTemplateMapper(any(CCDCase.class), anyString(), any(LocalDate.class)))
            .thenReturn(docAssemblyTemplateBody);
        when(docAssemblyService
            .generateDocument(any(CCDCase.class), anyString(), any(DocAssemblyTemplateBody.class), anyString(), anyString(), anyString()))
            .thenReturn(ccdDocument);

        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(CITIZEN_DETAILS);

        paperResponseLetterService.createOCON9From(ccdCase, AUTHORISATION_TOKEN, LocalDate.now());

        verify(paperDefenceLetterBodyMapper).coverLetterTemplateMapper(ccdCase, CITIZEN_DETAILS.getFullName(),
            LocalDate.now());
        verify(docAssemblyService).generateDocument(any(CCDCase.class), eq(AUTHORISATION_TOKEN),
            eq(docAssemblyTemplateBody), anyString(), anyString(), anyString());
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
                .oconFormIndividualWithDQsMapper(any(CCDCase.class), any(LocalDate.class), any(String.class),
                    any(Boolean.class)))
                .thenReturn(docAssemblyTemplateBody);

            SearchCourtBySlugResponse searchCourtBySlugResponse = DataFactory.createSearchCourtBySlugResponseFromJson(SEARCH_BY_SLUG_NEWCASTLE_RESPONSE);
            when(courtFinderApi.getCourtDetailsFromNameSlug(any()))
                .thenReturn(searchCourtBySlugResponse);

            SearchCourtByPostcodeResponse searchCourtByPostcodeResponse = DataFactory.createSearchCourtByPostcodeResponseFromJson(SEARCH_BY_POSTCODE_NEWCASTLE_RESPONSE);
            when(courtFinderApi.findMoneyClaimCourtByPostcode(any()))
                .thenReturn(searchCourtByPostcodeResponse);

            paperResponseLetterService.createOconForm(ccdCase, claim, AUTHORISATION_TOKEN,
                EXTENDED_RESPONSE_DEADLINE, true);

            verify(docAssemblyService).generateDocument(eq(ccdCase), eq(AUTHORISATION_TOKEN),
                eq(docAssemblyTemplateBody), anyString(), anyString(), anyString());
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
                .oconFormSoleTraderWithDQsMapper(any(CCDCase.class), any(LocalDate.class), any(String.class),
                    any(Boolean.class)))
                .thenReturn(docAssemblyTemplateBody);
            SearchCourtBySlugResponse searchCourtBySlugResponse = DataFactory.createSearchCourtBySlugResponseFromJson(SEARCH_BY_SLUG_NEWCASTLE_RESPONSE);
            when(courtFinderApi.getCourtDetailsFromNameSlug(any()))
                .thenReturn(searchCourtBySlugResponse);

            SearchCourtByPostcodeResponse searchCourtByPostcodeResponse = DataFactory.createSearchCourtByPostcodeResponseFromJson(SEARCH_BY_POSTCODE_NEWCASTLE_RESPONSE);
            when(courtFinderApi.findMoneyClaimCourtByPostcode(any()))
                .thenReturn(searchCourtByPostcodeResponse);
            paperResponseLetterService.createOconForm(ccdCase, claim, AUTHORISATION_TOKEN,
                EXTENDED_RESPONSE_DEADLINE, true);
            verify(docAssemblyService).generateDocument(eq(ccdCase), eq(AUTHORISATION_TOKEN),
                eq(docAssemblyTemplateBody), anyString(), anyString(), anyString());
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
                .oconFormOrganisationWithDQsMapper(any(CCDCase.class), any(LocalDate.class), any(String.class),
                    any(Boolean.class)))
                .thenReturn(docAssemblyTemplateBody);
            SearchCourtBySlugResponse searchCourtBySlugResponse = DataFactory.createSearchCourtBySlugResponseFromJson(SEARCH_BY_SLUG_NEWCASTLE_RESPONSE);
            when(courtFinderApi.getCourtDetailsFromNameSlug(any()))
                .thenReturn(searchCourtBySlugResponse);

            SearchCourtByPostcodeResponse searchCourtByPostcodeResponse = DataFactory.createSearchCourtByPostcodeResponseFromJson(SEARCH_BY_POSTCODE_NEWCASTLE_RESPONSE);
            when(courtFinderApi.findMoneyClaimCourtByPostcode(any()))
                .thenReturn(searchCourtByPostcodeResponse);
            paperResponseLetterService.createOconForm(ccdCase, claim, AUTHORISATION_TOKEN,
                EXTENDED_RESPONSE_DEADLINE, true);
            verify(docAssemblyService).generateDocument(eq(ccdCase), eq(AUTHORISATION_TOKEN),
                eq(docAssemblyTemplateBody), anyString(), anyString(), anyString());
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
                .oconFormIndividualWithoutDQsMapper(any(CCDCase.class), any(LocalDate.class), any(Boolean.class)))
                .thenReturn(docAssemblyTemplateBody);
            SearchCourtBySlugResponse searchCourtBySlugResponse = DataFactory.createSearchCourtBySlugResponseFromJson(SEARCH_BY_SLUG_NEWCASTLE_RESPONSE);
            when(courtFinderApi.getCourtDetailsFromNameSlug(any()))
                .thenReturn(searchCourtBySlugResponse);

            SearchCourtByPostcodeResponse searchCourtByPostcodeResponse = DataFactory.createSearchCourtByPostcodeResponseFromJson(SEARCH_BY_POSTCODE_NEWCASTLE_RESPONSE);
            when(courtFinderApi.findMoneyClaimCourtByPostcode(any()))
                .thenReturn(searchCourtByPostcodeResponse);
            paperResponseLetterService.createOconForm(ccdCase, claim, AUTHORISATION_TOKEN,
                EXTENDED_RESPONSE_DEADLINE, true);
            verify(paperDefenceLetterBodyMapper)
                .oconFormIndividualWithoutDQsMapper(ccdCase, EXTENDED_RESPONSE_DEADLINE, DISABLEN9FORM);
            verify(docAssemblyService).generateDocument(eq(ccdCase), eq(AUTHORISATION_TOKEN),
                eq(docAssemblyTemplateBody), anyString(), anyString(), anyString());

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
                .oconFormSoleTraderWithoutDQsMapper(any(CCDCase.class), any(LocalDate.class), any(Boolean.class)))
                .thenReturn(docAssemblyTemplateBody);
            SearchCourtBySlugResponse searchCourtBySlugResponse = DataFactory.createSearchCourtBySlugResponseFromJson(SEARCH_BY_SLUG_NEWCASTLE_RESPONSE);
            when(courtFinderApi.getCourtDetailsFromNameSlug(any()))
                .thenReturn(searchCourtBySlugResponse);

            SearchCourtByPostcodeResponse searchCourtByPostcodeResponse = DataFactory.createSearchCourtByPostcodeResponseFromJson(SEARCH_BY_POSTCODE_NEWCASTLE_RESPONSE);
            when(courtFinderApi.findMoneyClaimCourtByPostcode(any()))
                .thenReturn(searchCourtByPostcodeResponse);
            paperResponseLetterService.createOconForm(ccdCase, claim, AUTHORISATION_TOKEN,
                EXTENDED_RESPONSE_DEADLINE, true);
            verify(paperDefenceLetterBodyMapper)
                .oconFormSoleTraderWithoutDQsMapper(ccdCase, EXTENDED_RESPONSE_DEADLINE, DISABLEN9FORM);
            verify(docAssemblyService).generateDocument(eq(ccdCase), eq(AUTHORISATION_TOKEN),
                eq(docAssemblyTemplateBody), anyString(), anyString(), anyString());

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
                .oconFormOrganisationWithoutDQsMapper(any(CCDCase.class), any(LocalDate.class), any(Boolean.class)))
                .thenReturn(docAssemblyTemplateBody);
            SearchCourtBySlugResponse searchCourtBySlugResponse = DataFactory.createSearchCourtBySlugResponseFromJson(SEARCH_BY_SLUG_NEWCASTLE_RESPONSE);
            when(courtFinderApi.getCourtDetailsFromNameSlug(any()))
                .thenReturn(searchCourtBySlugResponse);

            SearchCourtByPostcodeResponse searchCourtByPostcodeResponse = DataFactory.createSearchCourtByPostcodeResponseFromJson(SEARCH_BY_POSTCODE_NEWCASTLE_RESPONSE);
            when(courtFinderApi.findMoneyClaimCourtByPostcode(any()))
                .thenReturn(searchCourtByPostcodeResponse);
            paperResponseLetterService.createOconForm(ccdCase, claim, AUTHORISATION_TOKEN,
                EXTENDED_RESPONSE_DEADLINE, true);
            verify(paperDefenceLetterBodyMapper)
                .oconFormOrganisationWithoutDQsMapper(ccdCase, EXTENDED_RESPONSE_DEADLINE, DISABLEN9FORM);
            verify(docAssemblyService).generateDocument(eq(ccdCase), eq(AUTHORISATION_TOKEN),
                eq(docAssemblyTemplateBody), anyString(), anyString(), anyString());

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
