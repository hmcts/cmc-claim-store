package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.getWithClaimantResponseRejectionForPartAdmissionAndMediation;

@RunWith(MockitoJUnitRunner.class)
public class MediationCSVGeneratorTest {

    private static final String AUTHORISATION = "Bearer: aaa";

    private MediationCSVGenerator mediationCSVGenerator;

    @Mock
    private CaseRepository mockCaseRepository;

    private List<Claim> mediationClaims = new ArrayList<>();

    @Before
    public void setUp () {
        mediationCSVGenerator = new MediationCSVGenerator(mockCaseRepository);

        when(mockCaseRepository.getMediationClaims(AUTHORISATION, LocalDate.now()))
            .thenReturn(mediationClaims);
    }

    @Test
    public void shouldCreateMediationForClaim () {
        mediationClaims.add(getWithClaimantResponseRejectionForPartAdmissionAndMediation());

        String expected = "4,1,4,5,000CM001,80.89,1,Mediation Contact Person,07999999999\r\n" +
            "4,1,4,5,000CM001,80.89,2,Mediation Contact Person,07999999999\r\n";
        String mediationCSV = mediationCSVGenerator.createMediationCSV(AUTHORISATION, LocalDate.now());
        assertThat(mediationCSV).isEqualTo(expected);
    }

    @Test
    public void shouldCreateMediationCSVEvenWhenNoClaimsWithMediation () {

        String expected = "null,null,null,null,null,null,null,null,null\r\n";
        String mediationCSV = mediationCSVGenerator.createMediationCSV(AUTHORISATION, LocalDate.now());
        assertThat(mediationCSV).isEqualTo(expected);
    }

//    @Test(expected = MediationCSVGenerationException.class)
//    public void shouldThrowIOExceptionWhenErrorGenerationMediationCSV () {
//    }

}
