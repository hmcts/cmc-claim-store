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
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ReDeterminationMapperTest {

    @Autowired
    private ReDeterminationMapper mapper;

    @Test(expected = NullPointerException.class)
    public void mapToShouldThrowExceptionWhenBuildersIsNull() {
        mapper.to(null, SampleClaim.getDefault());
    }

    @Test(expected = NullPointerException.class)
    public void mapToShouldThrowExceptionWhenClaimIsNull() {
        mapper.to(CCDDefendant.builder(), null);
    }

    @Test
    public void shouldMapReDeterminationToCCD() {
        //given
        Claim claim = SampleClaim.builder()
            .withReDetermination(ReDetermination.builder()
                .partyType(MadeBy.CLAIMANT)
                .explanation("Need money sooner")
                .build())
            .withReDeterminationRequestedAt(LocalDateTime.now())
            .build();

        //when
        CCDDefendant.CCDDefendantBuilder builder = CCDDefendant.builder();
        mapper.to(builder, claim);

        //then
        ReDetermination reDetermination = claim.getReDetermination().orElseThrow(AssertionError::new);
        CCDDefendant ccdDefendant = builder.build();
        assertThat(reDetermination.getExplanation()).isEqualTo(ccdDefendant.getReDeterminationExplaination());
        assertThat(reDetermination.getPartyType().name()).isEqualTo(ccdDefendant.getReDeterminationMadeBy().name());

        LocalDateTime reDeterminationAt = claim.getReDeterminationRequestedAt().orElseThrow(AssertionError::new);
        assertThat(reDeterminationAt).isEqualTo(ccdDefendant.getReDeterminationRequestedDate());
    }

    @Test
    public void shouldReturnNullWhenMapNullReDeterminationToCCD() {
        //given
        Claim claim = SampleClaim.builder().withReDetermination(null)
            .withReDeterminationRequestedAt(null)
            .build();

        //when
        CCDDefendant.CCDDefendantBuilder builder = CCDDefendant.builder();
        mapper.to(builder, claim);

        //then
        CCDDefendant ccdDefendant = builder.build();
        assertThat(ccdDefendant.getReDeterminationExplaination()).isBlank();
        assertThat(ccdDefendant.getReDeterminationMadeBy()).isNull();
        assertThat(ccdDefendant.getReDeterminationRequestedDate()).isNull();
    }

    @Test
    public void shouldReturnNullForBlankReDeterminationFromCCD() {
        //given
        CCDDefendant ccdDefendant = CCDDefendant.builder().build();
        Claim.ClaimBuilder builder = Claim.builder();

        //when
        mapper.from(builder, ccdDefendant);

        //then
        Claim claim = builder.build();
        assertThat(claim.getReDetermination().isPresent()).isFalse();
        assertThat(claim.getReDeterminationRequestedAt().isPresent()).isFalse();
    }

    @Test
    public void shouldMapReDeterminationFromCCD() {
        //given
        CCDDefendant ccdDefendant = SampleCCDDefendant.withReDetermination().build();
        Claim.ClaimBuilder builder = Claim.builder();

        //when
        mapper.from(builder, ccdDefendant);

        //then
        Claim claim = builder.build();
        ReDetermination reDetermination = claim.getReDetermination().orElseThrow(AssertionError::new);
        assertThat(reDetermination.getPartyType().name()).isEqualTo(ccdDefendant.getReDeterminationMadeBy().name());
        assertThat(reDetermination.getExplanation()).isEqualTo(ccdDefendant.getReDeterminationExplaination());

        LocalDateTime reDeterminationAt = claim.getReDeterminationRequestedAt().orElseThrow(AssertionError::new);
        assertThat(reDeterminationAt).isEqualTo(ccdDefendant.getReDeterminationRequestedDate());
    }
}
