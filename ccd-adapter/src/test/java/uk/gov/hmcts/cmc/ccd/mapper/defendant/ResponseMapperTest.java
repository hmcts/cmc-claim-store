package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
import uk.gov.hmcts.cmc.ccd.util.SampleCCDDefendant;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ResponseMapperTest {

    @Autowired
    private ResponseMapper mapper;

    @Test(expected = NullPointerException.class)
    public void mapToShouldThrowExceptionWhenBuildersIsNull() {
        mapper.to(null, SampleResponse.FullDefence.validDefaults());
    }

    @Test(expected = NullPointerException.class)
    public void mapToShouldThrowExceptionWhenResponseIsNull() {
        mapper.to(CCDDefendant.builder(), null);
    }

    @Test
    public void shouldMapFullDefenceResponseToCCD() {
        //given
        Response response = SampleResponse.FullDefence.validDefaults();

        //when
        CCDDefendant.CCDDefendantBuilder builder = CCDDefendant.builder();
        mapper.to(builder, response);

        //then
        assertThat(response).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapFullAdmissionResponseToCCD() {
        //given
        Response response = SampleResponse.FullAdmission.validDefaults();

        //when
        CCDDefendant.CCDDefendantBuilder builder = CCDDefendant.builder();
        mapper.to(builder, response);

        //then
        assertThat(response).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapPartAdmissionResponseToCCD() {
        //given
        Response response = SampleResponse.PartAdmission.validDefaults();

        //when
        CCDDefendant.CCDDefendantBuilder builder = CCDDefendant.builder();
        mapper.to(builder, response);

        //then
        assertThat(response).isEqualTo(builder.build());
    }

    @Test
    public void shouldMapFullDefenceResponseFromCCD() {
        //given
        CCDDefendant ccdDefendant = SampleCCDDefendant.withFullDefenceResponse().build();
        Claim.ClaimBuilder builder = Claim.builder();

        //when
        mapper.from(builder, ccdDefendant);

        //then
        assertThat(builder.build().getResponse().orElse(null)).isEqualTo(ccdDefendant);
    }

    @Test
    public void shouldMapFullDefenceResponseWithFreeMediationFromCCD() {
        //given
        CCDDefendant ccdDefendant = SampleCCDDefendant.withFullDefenceResponseAndFreeMediation().build();
        Claim.ClaimBuilder builder = Claim.builder();

        //when
        mapper.from(builder, ccdDefendant);

        //then
        assertThat(builder.build().getResponse().orElse(null)).isEqualTo(ccdDefendant);
    }

    @Test
    public void shouldMapFullAdmissionResponseFromCCD() {
        //given
        CCDDefendant ccdDefendant = SampleCCDDefendant.withFullAdmissionResponse().build();
        Claim.ClaimBuilder builder = Claim.builder();

        //when
        mapper.from(builder, ccdDefendant);

        //then
        assertThat(builder.build().getResponse().orElse(null)).isEqualTo(ccdDefendant);
    }

    @Test
    public void shouldMapPartAdmissionResponseFromCCD() {
        //given
        CCDDefendant ccdDefendant = SampleCCDDefendant.withPartAdmissionResponse().build();
        Claim.ClaimBuilder builder = Claim.builder();

        //when
        mapper.from(builder, ccdDefendant);

        //then
        assertThat(builder.build().getResponse().orElse(null)).isEqualTo(ccdDefendant);
    }
}
