package uk.gov.hmcts.cmc.claimstore.processors;

import com.fasterxml.jackson.core.type.TypeReference;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.claimstore.exceptions.InvalidApplicationException;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.JsonMapperFactory;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAddress;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountRange;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleEvidence;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestDate;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepresentative;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTimeline;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

public class JsonMapperTest {

    private JsonMapper processor = JsonMapperFactory.create();

    @Test
    public void shouldProcessClaimDataToJson() throws JSONException {
        //given
        InterestDate interestDate = SampleInterestDate.builder()
            .withDate(LocalDate.of(2015, 2, 2))
            .build();

        ClaimData input = SampleClaimData.builder()
            .withExternalId(UUID.fromString("9f49d8df-b734-4e86-aeb6-e22f0c2ca78d"))
            .withInterest(SampleInterest.builder()
                    .withType(Interest.InterestType.STANDARD)
                    .withRate(new BigDecimal("8"))
                    .withReason(null)
                    .withInterestDate(interestDate)
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
        String output = processor.toJson(input);

        //then
        String expected = new ResourceReader().read("/claim-application.json");
        JSONAssert.assertEquals(expected, output, STRICT);
    }

    @Test
    public void shouldProcessFromJson() {
        //given
        String input = new ResourceReader().read("/claim-application.json");

        //when
        ClaimData output = processor.fromJson(input, ClaimData.class);

        //then
        InterestDate interestDate = SampleInterestDate.builder()
            .withDate(LocalDate.of(2015, 2, 2))
            .build();

        ClaimData expected = SampleClaimData.builder()
            .withExternalId(UUID.fromString("9f49d8df-b734-4e86-aeb6-e22f0c2ca78d"))
            .withInterest(SampleInterest.builder()
                    .withType(Interest.InterestType.STANDARD)
                    .withRate(new BigDecimal("8"))
                    .withReason(null)
                    .withInterestDate(interestDate)
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
            .withTimeline(SampleTimeline.validDefaults())
            .withEvidence(SampleEvidence.validDefaults())
            .build();

        assertThat(output).isEqualTo(expected);
    }

    @Test
    public void shouldProcessLegalClaimFromJson() {
        //given
        String input = new ResourceReader().read("/legal-claim-application.json");

        //when
        ClaimData output = processor.fromJson(input, ClaimData.class);

        //then
        InterestDate interestDate = SampleInterestDate.builder()
                .withDate(LocalDate.of(2015, 2, 2))
                .build();

        ClaimData expected = SampleClaimData.builder()
            .withExternalId(UUID.fromString("9f49d8df-b734-4e86-aeb6-e22f0c2ca78d"))
            .withInterest(
                    SampleInterest.builder()
                            .withType(Interest.InterestType.STANDARD)
                            .withRate(new BigDecimal("8"))
                            .withReason(null)
                            .withInterestDate(interestDate)
                            .build())
            .withAmount(
                SampleAmountRange.builder()
                    .withHigherValue(BigDecimal.valueOf(123.56))
                    .withLowerValue(BigDecimal.valueOf(123.56))
                    .build())
            .withDefendant(
                SampleTheirDetails.builder()
                    .withRepresentative(SampleRepresentative.builder().build())
                    .withServiceAddress(SampleAddress.validDefaults())
                    .individualDetails())
            .withTimeline(null)
            .withPayment(null)
            .withEvidence(null)
            .build();
        assertThat(output).isEqualTo(expected);
    }

    @Test
    public void shouldProcessDependantResponseFromJson() {
        //given
        String input = new ResourceReader().read("/defendant-response.json");

        //when
        Response output = processor.fromJson(input, Response.class);

        //then
        Response expected = SampleResponse.validDefaults();
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

    @Test
    public void shouldConvertMapToCCDCase() {

        LocalDateTime timestamp = LocalDateTime.now();
        String uuid = UUID.randomUUID().toString();
        LocalDate date = LocalDate.now();

        Map<String, Object> data = new HashMap<>();
        data.put("id", "1");
        data.put("referenceNumber", "000MC001");
        data.put("submitterId", "2");
        data.put("submittedOn", timestamp);
        data.put("externalId", uuid);
        data.put("issuedOn", date);
        data.put("responseDeadline", date.plusDays(14));
        data.put("moreTimeRequested", CCDYesNoOption.NO.name());

        CCDCase ccdCase = processor.convertValue(data, CCDCase.class);

        CCDCase expected = CCDCase.builder()
            .id(1L)
            .referenceNumber("000MC001")
            .submitterId("2")
            .submittedOn(timestamp.format(ISO_DATE_TIME))
            .externalId(uuid)
            .issuedOn(date.format(ISO_DATE))
            .responseDeadline(date.plusDays(14))
            .moreTimeRequested(CCDYesNoOption.NO)
            .build();

        assertThat(ccdCase).isEqualTo(expected);

    }
}
