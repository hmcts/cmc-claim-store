package uk.gov.hmcts.cmc.claimstore.models.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.config.JacksonConfiguration;
import uk.gov.hmcts.cmc.claimstore.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleRepaymentPlan;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleTheirDetails;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceReader;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class CountyCourtJudgmentSerializationTest {

    private static final ObjectMapper mapper = new JacksonConfiguration().objectMapper();

    @Test
    public void shouldConvertCCJJsonToJavaForIndividual() throws IOException {

        //given
        String input = readJsonFromFile("/county-court-judgment/individual-defendant.json");
        TheirDetails defendant = SampleTheirDetails.builder().withRepresentative(null).individualDetails();
        CountyCourtJudgment expected = SampleCountyCourtJudgment.builder().withDefendant(defendant).build();

        //when
        CountyCourtJudgment other = jsonToModel(input);

        //then
        assertThat(expected).isEqualTo(other);
    }

    @Test
    public void shouldConvertCCJJsonToJavaForSoleTrader() throws IOException {

        //given
        String input = readJsonFromFile("/county-court-judgment/sole-trader-defendant.json");
        TheirDetails defendant = SampleTheirDetails.builder().withRepresentative(null).soleTraderDetails();
        CountyCourtJudgment expected = SampleCountyCourtJudgment.builder().withDefendant(defendant).build();

        //when
        CountyCourtJudgment other = jsonToModel(input);

        //then
        assertThat(expected).isEqualTo(other);
    }

    @Test
    public void shouldConvertCCJJsonToJavaForCompany() throws IOException {

        //given
        String input = readJsonFromFile("/county-court-judgment/company-defendant.json");
        TheirDetails defendant = SampleTheirDetails.builder().withRepresentative(null).companyDetails();
        CountyCourtJudgment expected = SampleCountyCourtJudgment.builder().withDefendant(defendant).build();

        //when
        CountyCourtJudgment other = jsonToModel(input);

        //then
        assertThat(expected).isEqualTo(other);
    }

    @Test
    public void shouldConvertCCJJsonToJavaForOrganisation() throws IOException {

        //given
        String input = readJsonFromFile("/county-court-judgment/organisation-defendant.json");
        TheirDetails defendant = SampleTheirDetails.builder()
            .withRepresentative(null).withCompaniesHouseNumber("1243").organisationDetails();
        CountyCourtJudgment expected = SampleCountyCourtJudgment.builder().withDefendant(defendant).build();

        //when
        CountyCourtJudgment other = jsonToModel(input);

        //then
        assertThat(expected).isEqualTo(other);
    }

    @Test
    public void shouldConvertCCJJsonToJavaPaidByInstalments() throws IOException {

        //given
        String input = readJsonFromFile("/county-court-judgment/by-installments.json");
        TheirDetails defendant = SampleTheirDetails.builder().withRepresentative(null).individualDetails();
        CountyCourtJudgment expected = SampleCountyCourtJudgment.builder()
            .withDefendant(defendant)
            .withRepaymentPlan(SampleRepaymentPlan.builder().build())
            .build();

        //when
        CountyCourtJudgment other = jsonToModel(input);

        //then
        assertThat(expected).isEqualTo(other);
    }

    @Test
    public void shouldConvertCCJJsonToJavaPaidFullBySetDate() throws IOException {

        //given
        String input = readJsonFromFile("/county-court-judgment/full-by-date.json");
        TheirDetails defendant = SampleTheirDetails.builder().withRepresentative(null).individualDetails();
        CountyCourtJudgment expected = SampleCountyCourtJudgment.builder()
            .withDefendant(defendant)
            .withPayBySetDate(LocalDate.of(2100, 10, 10))
            .build();

        //when
        CountyCourtJudgment other = jsonToModel(input);

        //then
        assertThat(expected).isEqualTo(other);
    }

    private static CountyCourtJudgment jsonToModel(final String input) throws IOException {
        return mapper.readValue(input, CountyCourtJudgment.class);
    }

    private static String readJsonFromFile(final String path) {
        return new ResourceReader().read(path);
    }
}
