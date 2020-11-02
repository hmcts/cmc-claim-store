package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperresponsetests;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.PaperDefenceLetterBodyMapper;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class PaperDefenceLetterBodyMapperTest {
    protected static final String AUTHORISATION_TOKEN = "Bearer token";
    private static final String CASEWORKER = "Caseworker name";
    private static final LocalDate EXTENDED_RESPONSE_DEADLINE = LocalDate.now();
    private static final String HEARING_COURT = "Shoreditch";

    @Mock
    private Clock clock;

    private PaperDefenceLetterBodyMapper paperDefenceLetterBodyMapper;
    private DocAssemblyTemplateBody.DocAssemblyTemplateBodyBuilder docAssemblyTemplateBodyBuilder;
    private CCDCase ccdCase;
    private CCDRespondent respondent;
    private CCDApplicant applicant;
    private CCDParty givenRespondent;
    private CCDAddress claimantAddress;
    private CCDAddress defendantAddress;
    private String partyName;

    @BeforeEach
    void setUp() {
        paperDefenceLetterBodyMapper
            = new PaperDefenceLetterBodyMapper();

        ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase.setCaseName("case name");
        ccdCase.setRespondents(
            ImmutableList.of(
                CCDCollectionElement.<CCDRespondent>builder()
                    .value(SampleData.getIndividualRespondentWithDQ())
                    .build()
            ));
        ccdCase.setApplicants(
            ImmutableList.of(
                CCDCollectionElement.<CCDApplicant>builder()
                    .value(SampleData.getCCDApplicantIndividual())
                    .build()
            ));

        respondent = ccdCase.getRespondents().get(0).getValue();
        applicant = ccdCase.getApplicants().get(0).getValue();
        givenRespondent = respondent.getClaimantProvidedDetail();
        claimantAddress = applicant.getPartyDetail().getCorrespondenceAddress() == null
            ? applicant.getPartyDetail().getPrimaryAddress() : applicant.getPartyDetail()
            .getCorrespondenceAddress();
        defendantAddress = givenRespondent.getCorrespondenceAddress() == null
            ? givenRespondent.getPrimaryAddress() : givenRespondent.getCorrespondenceAddress();
        partyName = respondent.getPartyName() != null
            ? respondent.getPartyName() :
            respondent.getClaimantProvidedPartyName();
    }

    private CCDCase getCCDCase(CCDCase ccdCase, String partyName) {

        return ccdCase.toBuilder()
            .respondents(ImmutableList.of(CCDCollectionElement.<CCDRespondent>builder()
                .value(ccdCase.getRespondents().get(0).getValue().toBuilder()
                    .partyDetail(ccdCase.getRespondents().get(0).getValue().getPartyDetail().toBuilder()
                        .correspondenceAddress(null)
                        .build())
                    .partyName(partyName)
                    .responseMoreTimeNeededOption(CCDYesNoOption.NO)
                    .build())
                .build()))
            .applicants(ImmutableList.of(CCDCollectionElement.<CCDApplicant>builder()
                .value(ccdCase.getApplicants().get(0).getValue().toBuilder()
                    .partyDetail(ccdCase.getApplicants().get(0).getValue().getPartyDetail().toBuilder()
                        .correspondenceAddress(null)
                        .build())
                    .build())
                .build()))
            .build();
    }

    private CCDCase getCCDCaseForClaimantProvidedDetails(CCDCase ccdCase, String partyName) {

        return ccdCase.toBuilder()
            .respondents(ImmutableList.of(CCDCollectionElement.<CCDRespondent>builder()
                .value(ccdCase.getRespondents().get(0).getValue().toBuilder()
                    .partyDetail(ccdCase.getRespondents().get(0).getValue().getPartyDetail().toBuilder()
                        .correspondenceAddress(null)
                        .primaryAddress(null)
                        .build())
                    .partyName(partyName)
                    .build())
                .build()))
            .applicants(ImmutableList.of(CCDCollectionElement.<CCDApplicant>builder()
                .value(ccdCase.getApplicants().get(0).getValue().toBuilder()
                    .partyDetail(ccdCase.getApplicants().get(0).getValue().getPartyDetail().toBuilder()
                        .correspondenceAddress(null)
                        .build())
                    .build())
                .build()))
            .build();
    }

    private CCDCase getCCDCaseForClaimantProvidedPrimaryAddressDetails(CCDCase ccdCase, String partyName) {

        return ccdCase.toBuilder()
            .respondents(ImmutableList.of(CCDCollectionElement.<CCDRespondent>builder()
                .value(ccdCase.getRespondents().get(0).getValue().toBuilder()
                    .partyDetail(ccdCase.getRespondents().get(0).getValue().getPartyDetail().toBuilder()
                        .correspondenceAddress(null)
                        .primaryAddress(null)
                        .build())
                    .claimantProvidedDetail(ccdCase.getRespondents().get(0).getValue().getPartyDetail().toBuilder()
                        .correspondenceAddress(null)
                        .build())
                    .partyName(partyName)
                    .build())
                .build()))
            .applicants(ImmutableList.of(CCDCollectionElement.<CCDApplicant>builder()
                .value(ccdCase.getApplicants().get(0).getValue().toBuilder()
                    .partyDetail(ccdCase.getApplicants().get(0).getValue().getPartyDetail().toBuilder()
                        .correspondenceAddress(null)
                        .build())
                    .build())
                .build()))
            .build();
    }

    @Nested
    @DisplayName("Test for cover letter")
    class CoverLetterTests {
        @Test
        void shouldMapTemplateBodyWhenCoverLetterForDefendantWhenPartyIsNotPresent() {

            CCDCase updatedCCDCase = getCCDCase(ccdCase, null);

            LocalDate currentDate = LocalDate.now();
            DocAssemblyTemplateBody requestBody = paperDefenceLetterBodyMapper
                .coverLetterTemplateMapper(updatedCCDCase, CASEWORKER, EXTENDED_RESPONSE_DEADLINE);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                .partyName(respondent.getClaimantProvidedPartyName())
                .partyAddress(defendantAddress)
                .claimantName(applicant.getPartyName())
                .currentDate(currentDate)
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .responseDeadline(respondent.getResponseDeadline())
                .extendedResponseDeadline(EXTENDED_RESPONSE_DEADLINE)
                .caseworkerName(CASEWORKER)
                .caseName(ccdCase.getCaseName())
                .build();
            assertThat(requestBody).isEqualTo(expectedBody);
        }

        @Test
        void shouldMapTemplateBodyWhenCoverLetterForDefendant() {

            CCDCase updatedCCDCase = getCCDCase(ccdCase, null);
            LocalDate currentDate = LocalDate.now();
            DocAssemblyTemplateBody requestBody = paperDefenceLetterBodyMapper
                .coverLetterTemplateMapper(updatedCCDCase, CASEWORKER, EXTENDED_RESPONSE_DEADLINE);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                .partyName(respondent.getClaimantProvidedPartyName())
                .partyAddress(defendantAddress)
                .claimantName(applicant.getPartyName())
                .currentDate(currentDate)
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .responseDeadline(respondent.getResponseDeadline())
                .extendedResponseDeadline(EXTENDED_RESPONSE_DEADLINE)
                .caseworkerName(CASEWORKER)
                .caseName(ccdCase.getCaseName())
                .moreTimeRequested(false)
                .build();
            assertThat(requestBody).isEqualTo(expectedBody);
        }
    }

    @Nested
    @DisplayName("Tests for ocon form")
    class OconFormTests {

        @Test
        void shouldMapCommonTemplateBody() {
            DocAssemblyTemplateBody requestBody = paperDefenceLetterBodyMapper
                .oconFormCommonTemplateMapper(ccdCase, EXTENDED_RESPONSE_DEADLINE);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .responseDeadline(respondent.getResponseDeadline())
                .extendedResponseDeadline(EXTENDED_RESPONSE_DEADLINE)
                .claimAmount(ccdCase.getTotalAmount())
                .partyName(partyName)
                .partyAddress(defendantAddress)
                .claimantName(applicant.getPartyName())
                .claimantPhone(applicant.getPartyDetail().getTelephoneNumber().getTelephoneNumber())
                .claimantEmail(applicant.getPartyDetail().getEmailAddress())
                .claimantAddress(claimantAddress)
                .build();
            assertThat(requestBody).isEqualTo(expectedBody);
        }

        @Test
        void shouldMapCommonTemplateBodyWhenPartyNameNotPresent() {
            CCDCase updatedCCDCase = getCCDCase(ccdCase, null);

            DocAssemblyTemplateBody requestBody = paperDefenceLetterBodyMapper
                .oconFormCommonTemplateMapper(updatedCCDCase, EXTENDED_RESPONSE_DEADLINE);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .responseDeadline(respondent.getResponseDeadline())
                .extendedResponseDeadline(EXTENDED_RESPONSE_DEADLINE)
                .claimAmount(ccdCase.getTotalAmount())
                .partyName(respondent.getClaimantProvidedPartyName())
                .partyAddress(defendantAddress)
                .claimantName(applicant.getPartyName())
                .claimantPhone(applicant.getPartyDetail().getTelephoneNumber().getTelephoneNumber())
                .claimantEmail(applicant.getPartyDetail().getEmailAddress())
                .claimantAddress(claimantAddress)
                .build();
            assertThat(requestBody).isEqualTo(expectedBody);
        }

        @Test
        void shouldMapCommonTemplateBodyWhenClaimantProvidedAddress() {

            CCDCase updatedCCDCase = getCCDCaseForClaimantProvidedDetails(ccdCase, null);

            DocAssemblyTemplateBody requestBody = paperDefenceLetterBodyMapper
                .oconFormCommonTemplateMapper(updatedCCDCase, EXTENDED_RESPONSE_DEADLINE);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .responseDeadline(respondent.getResponseDeadline())
                .extendedResponseDeadline(EXTENDED_RESPONSE_DEADLINE)
                .claimAmount(ccdCase.getTotalAmount())
                .partyName(respondent.getClaimantProvidedPartyName())
                .partyAddress(defendantAddress)
                .claimantName(applicant.getPartyName())
                .claimantPhone(applicant.getPartyDetail().getTelephoneNumber().getTelephoneNumber())
                .claimantEmail(applicant.getPartyDetail().getEmailAddress())
                .claimantAddress(claimantAddress)
                .build();
            assertThat(requestBody).isEqualTo(expectedBody);
        }

        @Test
        void shouldMapCommonTemplateBodyWhenClaimantProvidedPrimaryAddress() {

            CCDCase updatedCCDCase = getCCDCaseForClaimantProvidedPrimaryAddressDetails(ccdCase, null);

            DocAssemblyTemplateBody requestBody = paperDefenceLetterBodyMapper
                .oconFormCommonTemplateMapper(updatedCCDCase, EXTENDED_RESPONSE_DEADLINE);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .responseDeadline(respondent.getResponseDeadline())
                .extendedResponseDeadline(EXTENDED_RESPONSE_DEADLINE)
                .claimAmount(ccdCase.getTotalAmount())
                .partyName(respondent.getClaimantProvidedPartyName())
                .partyAddress(defendantAddress)
                .claimantName(applicant.getPartyName())
                .claimantPhone(applicant.getPartyDetail().getTelephoneNumber().getTelephoneNumber())
                .claimantEmail(applicant.getPartyDetail().getEmailAddress())
                .claimantAddress(claimantAddress)
                .build();
            assertThat(requestBody).isEqualTo(expectedBody);
        }

        @Test
        void shouldMapTemplateBodyWhenIndividualWithDQs() {
            ccdCase.setPreferredDQCourt(HEARING_COURT);
            DocAssemblyTemplateBody requestBody = paperDefenceLetterBodyMapper
                .oconFormIndividualWithDQsMapper(ccdCase, EXTENDED_RESPONSE_DEADLINE, HEARING_COURT);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .responseDeadline(respondent.getResponseDeadline())
                .extendedResponseDeadline(EXTENDED_RESPONSE_DEADLINE)
                .claimAmount(ccdCase.getTotalAmount())
                .partyName(partyName)
                .partyAddress(defendantAddress)
                .claimantName(applicant.getPartyName())
                .claimantPhone(applicant.getPartyDetail().getTelephoneNumber().getTelephoneNumber())
                .claimantEmail(applicant.getPartyDetail().getEmailAddress())
                .claimantAddress(claimantAddress)
                .preferredCourt(HEARING_COURT)
                .build();

            assertThat(requestBody).isEqualTo(expectedBody);
        }

        @Test
        void shouldMapTemplateBodyWhenIndividualWithoutDQs() {
            ccdCase.setPreferredDQCourt(HEARING_COURT);
            DocAssemblyTemplateBody requestBody = paperDefenceLetterBodyMapper
                .oconFormIndividualWithoutDQsMapper(ccdCase, EXTENDED_RESPONSE_DEADLINE);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .responseDeadline(respondent.getResponseDeadline())
                .extendedResponseDeadline(EXTENDED_RESPONSE_DEADLINE)
                .claimAmount(ccdCase.getTotalAmount())
                .partyName(partyName)
                .partyAddress(defendantAddress)
                .claimantName(applicant.getPartyName())
                .claimantPhone(applicant.getPartyDetail().getTelephoneNumber().getTelephoneNumber())
                .claimantEmail(applicant.getPartyDetail().getEmailAddress())
                .claimantAddress(claimantAddress)
                .build();

            assertThat(requestBody).isEqualTo(expectedBody);
        }

        @Test
        void shouldMapTemplateBodyWhenCompanyWithDQs() {
            ccdCase.setPreferredDQCourt(HEARING_COURT);
            DocAssemblyTemplateBody requestBody = paperDefenceLetterBodyMapper
                .oconFormOrganisationWithDQsMapper(ccdCase, EXTENDED_RESPONSE_DEADLINE, HEARING_COURT);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .responseDeadline(respondent.getResponseDeadline())
                .extendedResponseDeadline(EXTENDED_RESPONSE_DEADLINE)
                .claimAmount(ccdCase.getTotalAmount())
                .partyName(partyName)
                .partyAddress(defendantAddress)
                .claimantName(applicant.getPartyName())
                .claimantPhone(applicant.getPartyDetail().getTelephoneNumber().getTelephoneNumber())
                .claimantEmail(applicant.getPartyDetail().getEmailAddress())
                .claimantAddress(claimantAddress)
                .preferredCourt(HEARING_COURT)
                .organisationName(applicant.getPartyName())
                .build();

            assertThat(requestBody).isEqualTo(expectedBody);
        }

        @Test
        void shouldMapTemplateBodyWhenCompanyWithoutDQs() {
            DocAssemblyTemplateBody requestBody = paperDefenceLetterBodyMapper
                .oconFormOrganisationWithoutDQsMapper(ccdCase, EXTENDED_RESPONSE_DEADLINE);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .responseDeadline(respondent.getResponseDeadline())
                .extendedResponseDeadline(EXTENDED_RESPONSE_DEADLINE)
                .claimAmount(ccdCase.getTotalAmount())
                .partyName(partyName)
                .partyAddress(defendantAddress)
                .claimantName(applicant.getPartyName())
                .claimantPhone(applicant.getPartyDetail().getTelephoneNumber().getTelephoneNumber())
                .claimantEmail(applicant.getPartyDetail().getEmailAddress())
                .claimantAddress(claimantAddress)
                .organisationName(applicant.getPartyName())
                .build();

            assertThat(requestBody).isEqualTo(expectedBody);
        }

        @Test
        void shouldMapTemplateBodyWhenSoleTraderWithDQs() {
            ccdCase.setPreferredDQCourt(HEARING_COURT);
            DocAssemblyTemplateBody requestBody = paperDefenceLetterBodyMapper
                .oconFormSoleTraderWithDQsMapper(ccdCase, EXTENDED_RESPONSE_DEADLINE, HEARING_COURT);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .responseDeadline(respondent.getResponseDeadline())
                .extendedResponseDeadline(EXTENDED_RESPONSE_DEADLINE)
                .claimAmount(ccdCase.getTotalAmount())
                .partyName(partyName)
                .partyAddress(defendantAddress)
                .claimantName(applicant.getPartyName())
                .claimantPhone(applicant.getPartyDetail().getTelephoneNumber().getTelephoneNumber())
                .claimantEmail(applicant.getPartyDetail().getEmailAddress())
                .claimantAddress(claimantAddress)
                .preferredCourt(HEARING_COURT)
                .soleTradingTraderName(respondent.getPartyDetail().getBusinessName())
                .build();

            assertThat(requestBody).isEqualTo(expectedBody);

        }

        @Test
        void shouldMapTemplateBodyWhenSoleTraderWithoutDQs() {
            DocAssemblyTemplateBody requestBody = paperDefenceLetterBodyMapper
                .oconFormSoleTraderWithoutDQsMapper(ccdCase, EXTENDED_RESPONSE_DEADLINE);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                .responseDeadline(respondent.getResponseDeadline())
                .extendedResponseDeadline(EXTENDED_RESPONSE_DEADLINE)
                .claimAmount(ccdCase.getTotalAmount())
                .partyName(partyName)
                .partyAddress(defendantAddress)
                .claimantName(applicant.getPartyName())
                .claimantPhone(applicant.getPartyDetail().getTelephoneNumber().getTelephoneNumber())
                .claimantEmail(applicant.getPartyDetail().getEmailAddress())
                .claimantAddress(claimantAddress)
                .soleTradingTraderName(respondent.getPartyDetail().getBusinessName())
                .build();

            assertThat(requestBody).isEqualTo(expectedBody);
        }
    }
}
