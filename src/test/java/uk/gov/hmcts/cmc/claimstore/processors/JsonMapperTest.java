package uk.gov.hmcts.cmc.claimstore.processors;

import com.fasterxml.jackson.core.type.TypeReference;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderGenerationData;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType.BOTH;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDHearingDurationType.HALF_HOUR;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType.EYEWITNESS;

public class JsonMapperTest {

    private final JsonMapper processor = JsonMapperFactory.create();

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
                .withDateOfBirth(null)
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
                .withDateOfBirth(null)
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
                    .lowerValue(BigDecimal.valueOf(123.56))
                    .higherValue(BigDecimal.valueOf(123.56))
                    .build())
            .withDefendant(
                SampleTheirDetails.builder()
                    .withRepresentative(SampleRepresentative.builder().build())
                    .withServiceAddress(SampleAddress.builder().build())
                    .withDateOfBirth(null)
                    .withName(null)
                    .individualDetails())
            .withTimeline(null)
            .withPayment(null)
            .withEvidence(null)
            .build();
        assertThat(output).isEqualTo(expected);
    }

    @Test
    public void shouldProcessDependantResponseFullDefenceFromJson() {
        //given
        String input = new ResourceReader().read("/defendant-response-full-defence.json");

        //when
        Response output = processor.fromJson(input, Response.class);

        //then
        Response expected = SampleResponse.validDefaults();
        Assert.assertEquals(output, expected);
    }

    @Test
    public void shouldProcessDependantResponseFullAdmissionFromJson() {
        //given
        String input = new ResourceReader().read("/defendant-response-full-admission.json");

        //when
        Response output = processor.fromJson(input, Response.class);

        //then
        Response expected = SampleResponse.FullAdmission.builder().build();
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

        Map<String, Object> data = new HashMap<>();
        data.put("id", "1");
        data.put("previousServiceCaseReference", "000MC001");
        data.put("submitterId", "2");
        data.put("submittedOn", timestamp);
        data.put("externalId", uuid);

        CCDCase ccdCase = processor.convertValue(data, CCDCase.class);

        CCDCase expected = CCDCase.builder()
            .id(1L)
            .previousServiceCaseReference("000MC001")
            .submitterId("2")
            .submittedOn(timestamp)
            .externalId(uuid)
            .build();

        assertThat(ccdCase).isEqualTo(expected);
    }

    @Test
    public void shouldConvertMapToCCDOrderGenerationData() {

        Map<String, Object> data = new HashMap<>();
        data.put("docUploadForParty", null);
        data.put("eyewitnessUploadForParty", "BOTH");
        data.put("eyewitnessUploadDeadline", "2019-06-03");
        data.put("docUploadDeadline", null);
        data.put("paperDetermination", "No");
        data.put("hearingCourt", "BIRMINGHAM");
        data.put("otherDirections", Collections.emptyList());
        data.put("directionList", ImmutableList.of("EYEWITNESS"));
        data.put("estimatedHearingDuration", "HALF_HOUR");

        CCDOrderGenerationData ccdOrderGenerationData = processor.convertValue(data, CCDOrderGenerationData.class);

        CCDOrderGenerationData expected = CCDOrderGenerationData.builder()
            .directionList(Collections.singletonList(EYEWITNESS))
            .otherDirections(Collections.emptyList())
            .paperDetermination(NO)
            .eyewitnessUploadDeadline(LocalDate.parse("2019-06-03"))
            .hearingCourt("BIRMINGHAM")
            .eyewitnessUploadForParty(BOTH)
            .estimatedHearingDuration(HALF_HOUR)
            .build();

        assertThat(ccdOrderGenerationData).isEqualTo(expected);

    }
}
