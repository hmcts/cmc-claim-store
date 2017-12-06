package uk.gov.hmcts.cmc.claimstore.processors;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.hmcts.cmc.claimstore.exceptions.InvalidApplicationException;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.JsonMapperFactory;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountRange;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestDate;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepresentative;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

public class JsonMapperTest {

    private JsonMapper processor = JsonMapperFactory.create();

    @Test
    public void shouldProcessClaimDataToJson() throws Exception {
        //given
        final ClaimData input = SampleClaimData.builder()
            .withExternalId(UUID.fromString("9f49d8df-b734-4e86-aeb6-e22f0c2ca78d"))
            .withInterestDate(SampleInterestDate.builder()
                .withDate(LocalDate.of(2015, 2, 2))
                .build())
            .withExternalReferenceNumber(null)
            .withPreferredCourt(null)
            .withFeeAccountNumber(null)
            .withHousingDisrepair(null)
            .withPersonalInjury(null)
            .withStatementOfTruth(null)
            .clearClaimants()
            .addClaimant(SampleParty.builder().withRepresentative(null).individual())
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
            .withExternalId(UUID.fromString("9f49d8df-b734-4e86-aeb6-e22f0c2ca78d"))
            .withInterestDate(SampleInterestDate.builder()
                .withDate(LocalDate.of(2015, 2, 2))
                .build())
            .withExternalReferenceNumber(null)
            .withPreferredCourt(null)
            .withFeeAccountNumber(null)
            .withHousingDisrepair(null)
            .withPersonalInjury(null)
            .withStatementOfTruth(null)
            .clearClaimants()
            .addClaimant(SampleParty.builder().withRepresentative(null).individual())
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
            .withExternalId(UUID.fromString("9f49d8df-b734-4e86-aeb6-e22f0c2ca78d"))
            .withInterestDate(
                SampleInterestDate.builder()
                    .withDate(LocalDate.of(2015, 2, 2))
                    .build()
            ).withAmount(
                SampleAmountRange.builder()
                    .withHigherValue(BigDecimal.valueOf(123.56))
                    .withLowerValue(BigDecimal.valueOf(123.56))
                    .build()
            ).withDefendant(
                SampleTheirDetails.builder()
                    .withRepresentative(SampleRepresentative.builder().build())
                    .withServiceAddress(SampleAddress.validDefaults())
                    .individualDetails()
            ).build();
        assertThat(output).isEqualTo(expected);
    }

    @Test
    public void shouldProcessDependantResponseFromJson() throws Exception {
        //given
        final String input = new ResourceReader().read("/defendant-response.json");

        //when
        final Response output = processor.fromJson(input, Response.class);

        //then
        final Response expected = SampleResponse.validDefaults();
        assertThat(output).isEqualTo(expected);
    }

    @Test(expected = InvalidApplicationException.class)
    public void shouldThrowExceptionOnInvalidJson() {
        processor.fromJson("{asads:", Response.class);
    }

    @Test(expected = InvalidApplicationException.class)
    public void shouldThrowExceptionOnInvalidJsonWithTypeReference() {
        processor.fromJson("{asads:", new TypeReference<List<Response>>() {
        });
    }

}
