package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDMapperConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.JsonMapperFactory;
import uk.gov.hmcts.cmc.claimstore.services.WorkingDayIndicator;
import uk.gov.hmcts.cmc.claimstore.services.bankholidays.NonWorkingDaysCollection;
import uk.gov.hmcts.cmc.claimstore.services.bankholidays.PublicHolidaysCollection;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.ResponseMethod;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponseWithDQ;

@SpringBootTest
@ContextConfiguration(classes = CCDMapperConfig.class)
@RunWith(SpringRunner.class)
class CaseDetailsConverterTest {

    private CaseDetailsConverter caseDetailsConverter;

    @Autowired
    private CaseMapper caseMapper;

    @Mock
    private PublicHolidaysCollection publicHolidaysCollection;

    @Mock
    private NonWorkingDaysCollection nonWorkingDaysCollection;

    @Nested
    class CtscDisabledTests {

        @BeforeEach
        void setup() {
            caseDetailsConverter = new CaseDetailsConverter(caseMapper,
                JsonMapperFactory.create(),
                new WorkingDayIndicator(publicHolidaysCollection, nonWorkingDaysCollection),
                33,
                false);
        }

        @Test
        void convertsCaseDetailsToCCDCase() {
            CaseDetails caseDetails = successfulCoreCaseDataStoreSubmitResponse();
            CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
            assertThat(ccdCase.getId()).isEqualTo(caseDetails.getId());
            assertThat(ccdCase.getState()).isEqualTo(caseDetails.getState());
        }

        @Test
        void convertsCaseDetailsToClaim() {
            CaseDetails caseDetails = successfulCoreCaseDataStoreSubmitResponse();
            Claim claim = caseDetailsConverter.extractClaim(caseDetails);
            assertThat(claim.getId()).isEqualTo(caseDetails.getId());
            assertThat(claim.getState().getValue()).isEqualTo(caseDetails.getState());
        }

        @Test
        void convertsCaseDetailsToClaimWithIntentionToProceedDeadline() {
            CaseDetails caseDetails = successfulCoreCaseDataStoreSubmitResponse();
            Claim claim = caseDetailsConverter.extractClaim(caseDetails);
            assertThat(claim.getIntentionToProceedDeadline()).isNotNull();
        }

        @Test
        void convertsCaseDetailsToClaimWithoutResponseMethod() {
            CaseDetails caseDetails = successfulCoreCaseDataStoreSubmitResponseWithDQ();
            Claim claim = caseDetailsConverter.extractClaim(caseDetails);
            assertThat(claim.getResponse().get().getResponseMethod()).isNotPresent();
        }
    }

    @Nested
    class CtscEnabledTests {

        @BeforeEach
        void setUp() {
            caseDetailsConverter = new CaseDetailsConverter(caseMapper,
                JsonMapperFactory.create(),
                new WorkingDayIndicator(publicHolidaysCollection, nonWorkingDaysCollection),
                33,
                true);
        }

        @Test
        void convertsCaseDetailsToClaimWithResponseMethod() {
            CaseDetails caseDetails = successfulCoreCaseDataStoreSubmitResponseWithDQ();
            Claim claim = caseDetailsConverter.extractClaim(caseDetails);
            assertThat(claim.getResponse().get().getResponseMethod().get()).isEqualTo(ResponseMethod.DIGITAL);
        }
    }
}
