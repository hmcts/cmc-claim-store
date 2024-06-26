package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleCCDDefendant;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@ExtendWith(SpringExtension.class)
public class ResponseMapperTest {

    @Autowired
    private ResponseMapper mapper;

    @Test
    public void mapToShouldThrowExceptionWhenBuildersIsNull() {
        assertThrows(NullPointerException.class, () -> {
            mapper.to(null, SampleResponse.FullDefence.validDefaults(), CCDParty.builder());
        });
    }

    @Test
    public void mapToShouldThrowExceptionWhenResponseIsNull() {
        assertThrows(NullPointerException.class, () -> {
            mapper.to(CCDRespondent.builder(), null, CCDParty.builder());
        });
    }

    @Test
    public void shouldMapFullDefenceResponseToCCD() {
        //given
        Response response = SampleResponse.FullDefence.validDefaults();

        //when
        CCDRespondent.CCDRespondentBuilder builder = CCDRespondent.builder();
        mapper.to(builder, response, CCDParty.builder());

        //then
        assertThat(response).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapFullAdmissionResponseToCCD() {
        //given
        Response response = SampleResponse.FullAdmission.validDefaults();

        //when
        CCDRespondent.CCDRespondentBuilder builder = CCDRespondent.builder();
        mapper.to(builder, response, CCDParty.builder());

        //then
        assertThat(response).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapPartAdmissionResponseToCCD() {
        //given
        Response response = SampleResponse.PartAdmission.validDefaults();

        //when
        CCDRespondent.CCDRespondentBuilder builder = CCDRespondent.builder();
        mapper.to(builder, response, CCDParty.builder());

        //then
        assertThat(response).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapFullDefenceResponseFromCCD() {
        //given
        CCDRespondent ccdRespondent = SampleCCDDefendant.withFullDefenceResponse().build();
        Claim.ClaimBuilder builder = Claim.builder();

        CCDCollectionElement<CCDRespondent> respondentElement = CCDCollectionElement.<CCDRespondent>builder()
            .value(ccdRespondent)
            .id(UUID.randomUUID().toString())
            .build();
        //when

        mapper.from(builder, respondentElement);

        //then
        assertThat(builder.build().getResponse().orElse(null)).isEqualTo(ccdRespondent);
    }

    @Test
    public void shouldMapFullDefenceResponseWithFreeMediationFromCCD() {
        //given
        CCDRespondent ccdRespondent = SampleCCDDefendant.withFullDefenceResponseAndFreeMediation().build();
        Claim.ClaimBuilder builder = Claim.builder();

        CCDCollectionElement<CCDRespondent> respondentElement = CCDCollectionElement.<CCDRespondent>builder()
            .value(ccdRespondent)
            .id(UUID.randomUUID().toString())
            .build();
        //when
        mapper.from(builder, respondentElement);

        //then
        assertThat(builder.build().getResponse().orElse(null)).isEqualTo(ccdRespondent);
    }

    @Test
    public void shouldMapFullAdmissionResponseFromCCD() {
        //given
        CCDRespondent ccdRespondent = SampleCCDDefendant.withFullAdmissionResponse().build();
        Claim.ClaimBuilder builder = Claim.builder();

        CCDCollectionElement<CCDRespondent> respondentElement = CCDCollectionElement.<CCDRespondent>builder()
            .value(ccdRespondent)
            .id(UUID.randomUUID().toString())
            .build();
        //when
        mapper.from(builder, respondentElement);

        //then
        assertThat(builder.build().getResponse().orElse(null)).isEqualTo(ccdRespondent);
    }

    @Test
    public void shouldMapPartAdmissionResponseFromCCD() {
        //given
        CCDRespondent ccdRespondent = SampleCCDDefendant.withPartAdmissionResponse().build();
        Claim.ClaimBuilder builder = Claim.builder();

        CCDCollectionElement<CCDRespondent> respondentElement = CCDCollectionElement.<CCDRespondent>builder()
            .value(ccdRespondent)
            .id(UUID.randomUUID().toString())
            .build();
        //when
        mapper.from(builder, respondentElement);

        //then
        assertThat(builder.build().getResponse().orElse(null)).isEqualTo(ccdRespondent);
    }
}
