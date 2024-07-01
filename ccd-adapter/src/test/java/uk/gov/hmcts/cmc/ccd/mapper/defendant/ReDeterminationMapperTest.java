package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleCCDDefendant;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@ExtendWith(SpringExtension.class)
public class ReDeterminationMapperTest {

    @Autowired
    private ReDeterminationMapper mapper;

    @Test
    public void mapToShouldThrowExceptionWhenBuildersIsNull() {
        assertThrows(NullPointerException.class, () -> {
            mapper.to(null, SampleClaim.getDefault());
        });
    }

    @Test
    public void mapToShouldThrowExceptionWhenClaimIsNull() {
        assertThrows(NullPointerException.class, () -> {
            mapper.to(CCDRespondent.builder(), null);
        });
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
        CCDRespondent.CCDRespondentBuilder builder = CCDRespondent.builder();
        mapper.to(builder, claim);
        CCDRespondent ccdRespondent = builder.build();

        //then
        ReDetermination reDetermination = claim.getReDetermination()
            .orElseThrow(() -> new AssertionError("Missing redetermination"));
        assertThat(reDetermination.getExplanation()).isEqualTo(ccdRespondent.getRedeterminationExplanation());
        assertThat(reDetermination.getPartyType().name()).isEqualTo(ccdRespondent.getRedeterminationMadeBy().name());

        assertThat(claim.getReDeterminationRequestedAt())
            .contains(ccdRespondent.getRedeterminationRequestedDate());
    }

    @Test
    public void shouldReturnNullWhenMapNullReDeterminationToCCD() {
        //given
        Claim claim = SampleClaim.builder().withReDetermination(null)
            .withReDeterminationRequestedAt(null)
            .build();

        //when
        CCDRespondent.CCDRespondentBuilder builder = CCDRespondent.builder();
        mapper.to(builder, claim);
        CCDRespondent ccdRespondent = builder.build();

        //then
        assertThat(ccdRespondent.getRedeterminationExplanation()).isBlank();
        assertThat(ccdRespondent.getRedeterminationMadeBy()).isNull();
        assertThat(ccdRespondent.getRedeterminationRequestedDate()).isNull();
    }

    @Test
    public void shouldReturnNullForBlankReDeterminationFromCCD() {
        //given
        CCDRespondent ccdRespondent = CCDRespondent.builder().build();
        Claim.ClaimBuilder builder = Claim.builder();

        //when
        mapper.from(builder, ccdRespondent);
        Claim claim = builder.build();

        //then
        assertThat(claim.getReDetermination().isPresent()).isFalse();
        assertThat(claim.getReDeterminationRequestedAt().isPresent()).isFalse();
    }

    @Test
    public void shouldMapReDeterminationFromCCD() {
        //given
        CCDRespondent ccdRespondent = SampleCCDDefendant.withReDetermination().build();
        Claim.ClaimBuilder builder = Claim.builder();

        //when
        mapper.from(builder, ccdRespondent);
        Claim claim = builder.build();

        //then
        ReDetermination reDetermination = claim.getReDetermination()
            .orElseThrow(() -> new AssertionError("Missing redetermination"));
        assertThat(reDetermination.getPartyType().name()).isEqualTo(ccdRespondent.getRedeterminationMadeBy().name());
        assertThat(reDetermination.getExplanation()).isEqualTo(ccdRespondent.getRedeterminationExplanation());

        assertThat(claim.getReDeterminationRequestedAt()).contains(ccdRespondent.getRedeterminationRequestedDate());
    }
}
