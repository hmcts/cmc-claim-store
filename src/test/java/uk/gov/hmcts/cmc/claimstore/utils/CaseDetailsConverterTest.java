package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDMapperConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.JsonMapperFactory;
import uk.gov.hmcts.cmc.claimstore.services.WorkingDayIndicator;
import uk.gov.hmcts.cmc.claimstore.services.bankholidays.NonWorkingDaysCollection;
import uk.gov.hmcts.cmc.claimstore.services.bankholidays.PublicHolidaysCollection;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;

@SpringBootTest
@ContextConfiguration(classes = CCDMapperConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CaseDetailsConverterTest {

    private CaseDetailsConverter caseDetailsConverter;

    @Autowired
    private CaseMapper caseMapper;

    @Mock
    private PublicHolidaysCollection publicHolidaysCollection;

    @Mock
    private NonWorkingDaysCollection nonWorkingDaysCollection;

    @Before
    public void setup() {
        caseDetailsConverter = new CaseDetailsConverter(caseMapper,
            JsonMapperFactory.create(),
            new WorkingDayIndicator(publicHolidaysCollection, nonWorkingDaysCollection),
            33);
    }

    @Test
    public void convertsCaseDetailsToCCDCase() {
        CaseDetails caseDetails = successfulCoreCaseDataStoreSubmitResponse();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        assertThat(ccdCase.getId()).isEqualTo(caseDetails.getId());
        assertThat(ccdCase.getState()).isEqualTo(caseDetails.getState());
    }

    @Test
    public void convertsCaseDetailsToClaim() {
        CaseDetails caseDetails = successfulCoreCaseDataStoreSubmitResponse();
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        assertThat(claim.getId()).isEqualTo(caseDetails.getId());
        assertThat(claim.getState().getValue()).isEqualTo(caseDetails.getState());
    }

    @Test
    public void convertsCaseDetailsToClaimWithIntentionToProceedDeadline() {
        CaseDetails caseDetails = successfulCoreCaseDataStoreSubmitResponse();
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        assertThat(claim.getIntentionToProceedDeadline()).isNotNull();
    }
}
