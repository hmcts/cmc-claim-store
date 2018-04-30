package uk.gov.hmcts.cmc.rpa.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import uk.gov.hmcts.cmc.rpa.config.RpaAdapterConfig;
import uk.gov.hmcts.cmc.rpa.domain.Case;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = RpaAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CaseMapperTest {

    @Autowired
    private CaseMapper rpaCaseMapper;

    @Test
    public void shouldMapIndividualCitizenClaimToRPA() throws JsonProcessingException {
        //given
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withCountyCourtJudgment(
                SampleCountyCourtJudgment.builder()
                    .withPaymentOptionImmediately()
                    .build()
            )
            .withIssuedOn(LocalDate.of(2018, 4, 26))
            .build();

        //when
        Case rpaCase = rpaCaseMapper.to(claim);

        String result = new ObjectMapper().writeValueAsString(rpaCase).trim();

        String expected = new ResourceReader().read("/individual_rpa_case.json").trim();

        //then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void shouldMapCompanyCitizenClaimToRPA() throws JsonProcessingException {
        //given
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder().withClaimant(SampleParty.builder().company()).build())
            .withIssuedOn(LocalDate.of(2018, 4, 26))
            .build();

        //when
        Case rpaCase = rpaCaseMapper.to(claim);

        String result = new ObjectMapper().writeValueAsString(rpaCase).trim();

        String expected = new ResourceReader().read("/company_rpa_case.json").trim();

        //then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void shouldMapSoleTraderCitizenClaimToRPA() throws JsonProcessingException {
        //given
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder().withClaimant(SampleParty.builder().soleTrader()).build())
            .withIssuedOn(LocalDate.of(2018, 4, 26))
            .build();

        //when
        Case rpaCase = rpaCaseMapper.to(claim);

        String result = new ObjectMapper().writeValueAsString(rpaCase).trim();

        String expected = new ResourceReader().read("/sole_trader_rpa_case.json").trim();

        //then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void shouldMapOrganisationCitizenClaimToRPA() throws JsonProcessingException {
        //given
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder().withClaimant(SampleParty.builder().organisation()).build())
            .withIssuedOn(LocalDate.of(2018, 4, 26))
            .build();

        //when
        Case rpaCase = rpaCaseMapper.to(claim);

        String result = new ObjectMapper().writeValueAsString(rpaCase).trim();
        System.out.println(result);

        String expected = new ResourceReader().read("/organisation_rpa_case.json").trim();

        //then
        assertThat(result).isEqualTo(expected);
    }
}
