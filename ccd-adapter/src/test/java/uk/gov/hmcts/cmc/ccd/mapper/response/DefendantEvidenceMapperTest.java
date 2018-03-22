package uk.gov.hmcts.cmc.ccd.mapper.response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDDefendantEvidence;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.ccd.mapper.DefendantEvidenceMapper;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceType.EXPERT_WITNESS;
import static uk.gov.hmcts.cmc.domain.models.evidence.EvidenceType.CORRESPONDENCE;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DefendantEvidenceMapperTest {

    @Autowired
    private DefendantEvidenceMapper mapper;

    @Test
    public void shouldMapDefendantEvidenceToCCD() {
        //given
        DefendantEvidence evidence = new DefendantEvidence(
            asList(new EvidenceRow(CORRESPONDENCE, "Work done")), "More info");

        //when
        CCDDefendantEvidence ccdDefendantEvidence = mapper.to(evidence);

        //then
        assertThat(evidence).isEqualTo(ccdDefendantEvidence);

    }

    @Test
    public void shouldMapDefendantEvidenceFromCCD() {
        //given
        CCDEvidenceRow evidenceRow = CCDEvidenceRow.builder()
            .description("My description")
            .type(EXPERT_WITNESS)
            .build();

        CCDDefendantEvidence ccdEvidence = CCDDefendantEvidence.builder()
            .rows(asList(CCDCollectionElement.<CCDEvidenceRow>builder().value(evidenceRow).build()))
            .comment("More ino")
            .build();

        //when
        DefendantEvidence evidence = mapper.from(ccdEvidence);

        //then
        assertThat(evidence).isEqualTo(ccdEvidence);
    }
}
