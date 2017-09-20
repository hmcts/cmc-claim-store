package uk.gov.hmcts.cmc.claimstore.processors;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleResponseData;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.models.ResponseData;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleAmountRange;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleInterestDate;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleTheirDetails;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.JsonMapperFactory;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceReader;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

public class JsonMapperTest {

    private JsonMapper processor = JsonMapperFactory.create();

    @Test
    public void shouldProcessClaimDataToJson() throws Exception {
        //given
        final ClaimData input = SampleClaimData.builder()
            .withInterestDate(SampleInterestDate.builder()
                .withDate(LocalDate.of(2015, 2, 2))
                .build())
            .withExternalReferenceNumber(null)
            .withPreferredCourt(null)
            .withFeeAccountNumber(null)
            .withHousingDisrepair(null)
            .withPersonalInjury(null)
            .withStatementOfTruth(null)
            .withClaimant(SampleParty.builder().withRepresentative(null).individual())
            .withDefendant(SampleTheirDetails.builder()
                .withRepresentative(null)
                .withServiceAddress(null)
                .individualDetails())
            .withFeeCode("X0012")
            .build();

        //when
        final String output = processor.toJson(input);

        //then
        final String expected = new ResourceReader().read("/claim-application.json");
        JSONAssert.assertEquals(expected, output, STRICT);
    }

    @Test
    public void shouldProcessFromJson() throws Exception {
        //given
        final String input = new ResourceReader().read("/claim-application.json");

        //when
        final ClaimData output = processor.fromJson(input, ClaimData.class);

        //then
        final ClaimData expected = SampleClaimData.builder()
            .withInterestDate(SampleInterestDate.builder()
                .withDate(LocalDate.of(2015, 2, 2))
                .build())
            .withExternalReferenceNumber(null)
            .withPreferredCourt(null)
            .withFeeAccountNumber(null)
            .withHousingDisrepair(null)
            .withPersonalInjury(null)
            .withStatementOfTruth(null)
            .withClaimant(SampleParty.builder().withRepresentative(null).individual())
            .withDefendant(SampleTheirDetails.builder()
                .withRepresentative(null)
                .withServiceAddress(null)
                .individualDetails())
            .build();

        assertThat(output).isEqualTo(expected);
    }

    @Test
    public void shouldProcessLegalClaimFromJson() throws Exception {
        //given
        final String input = new ResourceReader().read("/legal-claim-application.json");

        //when
        final ClaimData output = processor.fromJson(input, ClaimData.class);

        //then
        final ClaimData expected = SampleClaimData.builder()
            .withInterestDate(SampleInterestDate.builder()
                .withDate(LocalDate.of(2015, 2, 2))
                .build())
            .withAmount(SampleAmountRange.builder().withHigherValue(BigDecimal.valueOf(123.56))
                .withLowerValue(BigDecimal.valueOf(123.56)).build())
            .build();
        assertThat(output).isEqualTo(expected);
    }

    @Test
    public void shouldProcessDependantResponseFromJson() throws Exception {
        //given
        final String input = new ResourceReader().read("/defendant-response.json");

        //when
        final ResponseData output = processor.fromJson(input, ResponseData.class);

        //then
        final ResponseData expected = SampleResponseData.validDefaults();
        assertThat(output).isEqualTo(expected);
    }

}
