package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.exceptions.MediationCSVGenerationException;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.getWithClaimantResponseRejectionForPartAdmissionAndMediation;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.withNoResponse;

@RunWith(MockitoJUnitRunner.class)
public class MediationCSVGeneratorTest {

    private static final String AUTHORISATION = "Bearer: aaa";

    private MediationCSVGenerator mediationCSVGenerator;

    private static final String CRLF = "\r\n";

    private static final String REPORT_HEADER = "SITE_ID,CASE_TYPE,CHECK_LIST,PARTY_STATUS,CASE_NUMBER,AMOUNT,"
        + "PARTY_TYPE,COMPANY_NAME,CONTACT_NAME,CONTACT_NUMBER,CONTACT_EMAIL,PILOT" + CRLF;

    @Mock
    private CaseSearchApi caseSearchApi;

    private List<Claim> mediationClaims;

    @Before
    public void setUp() {
        mediationClaims = new ArrayList<>();
        mediationCSVGenerator = new MediationCSVGenerator(caseSearchApi, LocalDate.now(), AUTHORISATION);

        when(caseSearchApi.getMediationClaims(AUTHORISATION, LocalDate.now()))
            .thenReturn(mediationClaims);
    }

    @Test
    public void shouldCreateMediationForClaim() {
        mediationClaims.add(getWithClaimantResponseRejectionForPartAdmissionAndMediation());

        String expected = REPORT_HEADER
            + "5,1,4,5,000MC001,40.99,1,John Rambo,Mediation Contact Person,07999999999,claimant@mail.com,Yes"
            + CRLF
            + "5,1,4,5,000MC001,40.99,2,Dr. John Smith,Mediation Contact Person,07999999999,j.smith@example.com,Yes"
            + CRLF;
        mediationCSVGenerator.createMediationCSV();
        String mediationCSV = mediationCSVGenerator.getCsvData();
        assertThat(mediationCSV).isEqualTo(expected);
    }

    @Test
    public void shouldCreateMediationForClaimWithPilot() {
        Claim claimWithAmountMoreThan300 = getWithClaimantResponseRejectionForPartAdmissionAndMediation()
            .toBuilder()
            .claimData(SampleClaimData.submittedWithAmountMoreThanThousand()).build();
        mediationClaims.add(getWithClaimantResponseRejectionForPartAdmissionAndMediation());
        mediationClaims.add(claimWithAmountMoreThan300);

        String expected = REPORT_HEADER
            + "5,1,4,5,000MC001,40.99,1,John Rambo,Mediation Contact Person,07999999999,claimant@mail.com,Yes"
            + CRLF
            + "5,1,4,5,000MC001,40.99,2,Dr. John Smith,Mediation Contact Person,07999999999,j.smith@example.com,Yes"
            + CRLF
            + "5,1,4,5,000MC001,1000.99,1,John Rambo,Mediation Contact Person,07999999999,claimant@mail.com,No"
            + CRLF
            + "5,1,4,5,000MC001,1000.99,2,Dr. John Smith,Mediation Contact Person,07999999999,j.smith@example.com,No"
            + CRLF;
        mediationCSVGenerator.createMediationCSV();
        String mediationCSV = mediationCSVGenerator.getCsvData();
        assertThat(mediationCSV).isEqualTo(expected);
    }

    @Test
    public void shouldCreateMediationCSVEvenWhenNoClaimsWithMediation() {
        String expected = REPORT_HEADER + "null,null,null,null,null,null,null,null,null,null,null,null"
            + CRLF;
        mediationCSVGenerator.createMediationCSV();
        String mediationCSV = mediationCSVGenerator.getCsvData();
        assertThat(mediationCSV).isEqualTo(expected);
    }

    @Test
    public void shouldReportSuccessfulDataWhenAnyFail() {
        mediationClaims.add(getWithClaimantResponseRejectionForPartAdmissionAndMediation());
        mediationClaims.add(withNoResponse());
        mediationClaims.add(getWithClaimantResponseRejectionForPartAdmissionAndMediation());

        mediationCSVGenerator.createMediationCSV();
        String mediationCSV = mediationCSVGenerator.getCsvData();
        String[] csvLines = mediationCSV.split("[\\r\\n]+");

        assertThat(csvLines).hasSize(5);
        assertThat(mediationCSVGenerator.getProblematicRecords()).hasSize(1);
    }

    @Test(expected = MediationCSVGenerationException.class)
    public void shouldWrapProblemAsMediationException() {
        when(caseSearchApi.getMediationClaims(anyString(), any(LocalDate.class)))
            .thenThrow(new RuntimeException());

        mediationCSVGenerator.createMediationCSV();
    }
}
