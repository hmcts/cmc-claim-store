package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.PaperResponseTests;

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
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.services.DirectionOrderService;
import uk.gov.hmcts.cmc.claimstore.services.WorkingDayIndicator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.PaperDefenceLetterBodyMapper;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.UTC_ZONE;

@ExtendWith(MockitoExtension.class)
public class PaperDefenceLetterBodyMapperTest {
    protected static final String AUTHORISATION_TOKEN = "Bearer token";
    private static final String CASEWORKER = "Caseworker name";
    private static final LocalDate EXTENDED_RESPONSE_DEADLINE = LocalDate.now();

    @Mock
    private Clock clock;
    @Mock
    private DirectionOrderService directionOrderService;
    @Mock
    private WorkingDayIndicator workingDayIndicator;

    private PaperDefenceLetterBodyMapper paperDefenceLetterBodyMapper;
    private DocAssemblyTemplateBody.DocAssemblyTemplateBodyBuilder docAssemblyTemplateBodyBuilder;
    private CCDCase ccdCase;

    @BeforeEach
    void setUp() {
        paperDefenceLetterBodyMapper
                = new PaperDefenceLetterBodyMapper(clock);

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
    }

    @Nested
    @DisplayName("Tests for cover letter")
    class CoverLetterTests {
        @Test
        void shouldMapTemplateBodyWhenCoverLetterForDefendant() {

            CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();
            CCDParty givenRespondent = respondent.getClaimantProvidedDetail();
            CCDAddress defendantAddress = givenRespondent.getCorrespondenceAddress() == null
                    ? givenRespondent.getPrimaryAddress() : givenRespondent.getCorrespondenceAddress();
            CCDApplicant applicant = ccdCase.getApplicants().get(0).getValue();

            LocalDate currentDate = LocalDate.now(clock.withZone(UTC_ZONE));

            String partyName = respondent.getPartyName() != null
                    ? respondent.getPartyName() :
                    respondent.getClaimantProvidedPartyName();

            DocAssemblyTemplateBody requestBody = paperDefenceLetterBodyMapper
                    .coverLetterTemplateMapper(ccdCase, CASEWORKER, EXTENDED_RESPONSE_DEADLINE);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                    .partyName(partyName)
                    .partyAddress(defendantAddress)
                    .claimantName(applicant.getPartyName())
                    .currentDate(currentDate)
                    .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                    .responseDeadline(respondent.getResponseDeadline())
                    .updatedResponseDeadline(EXTENDED_RESPONSE_DEADLINE)
                    .caseworkerName(CASEWORKER)
                    .caseName(ccdCase.getCaseName())
                    .build();
            assertThat(requestBody).isEqualTo(expectedBody);
        }
    }

    @Nested
    @DisplayName("Tests for ocon form")
    class OconFormTests {
        @Test
        void shouldMapCommonTemplateBody() {
            CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();
            CCDApplicant applicant = ccdCase.getApplicants().get(0).getValue();
            CCDParty givenRespondent = respondent.getClaimantProvidedDetail();
            CCDAddress claimantAddress = applicant.getPartyDetail().getCorrespondenceAddress() == null
                    ? applicant.getPartyDetail().getPrimaryAddress() : applicant.getPartyDetail().getCorrespondenceAddress();
            //does this work if the defendant is a company
            CCDAddress defendantAddress = givenRespondent.getCorrespondenceAddress() == null
                    ? givenRespondent.getPrimaryAddress() : givenRespondent.getCorrespondenceAddress();

            String partyName = respondent.getPartyName() != null
                    ? respondent.getPartyName() :
                    respondent.getClaimantProvidedPartyName();

            DocAssemblyTemplateBody requestBody = paperDefenceLetterBodyMapper
                    .oconFormCommonTemplateMapper(ccdCase, EXTENDED_RESPONSE_DEADLINE);
            DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
                    .referenceNumber(ccdCase.getPreviousServiceCaseReference())
                    .responseDeadline(respondent.getResponseDeadline())
                    .updatedResponseDeadline(EXTENDED_RESPONSE_DEADLINE)
                    .claimAmount(ccdCase.getTotalAmount())
                    .partyName(partyName)
                    .partyAddress(defendantAddress)
                    .claimantName(applicant.getPartyName())
                    .claimantPhone(applicant.getPartyDetail().getTelephoneNumber().toString())
                    .claimantEmail(applicant.getPartyDetail().getEmailAddress())
                    .claimantAddress(claimantAddress)
                    .build();
            assertThat(requestBody).isEqualTo(expectedBody);
        }
        @Test
        void shouldMapTemplateBodyWhenIndividualWithDQs() {

        }
        @Test
        void shouldMapTemplateBodyWhenIndividualWithouthDQs() {

        }
        @Test
        void shouldMapTemplateBodyWhenCompanyWithDQs() {

        }
        @Test
        void shouldMapTemplateBodyWhenCompanyWithoutDQs() {

        }
        @Test
        void shouldMapTemplateBodyWhenSoleTraderWithDQs() {

        }
        @Test
        void shouldMapTemplateBodyWhenSoleTraderWithoutDQs() {

        }
    }
}
