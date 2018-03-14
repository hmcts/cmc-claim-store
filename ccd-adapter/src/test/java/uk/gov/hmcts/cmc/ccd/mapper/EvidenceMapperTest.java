package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidence;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceType;
import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.evidence.EvidenceType.CORRESPONDENCE;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class EvidenceMapperTest {

    @Autowired
    private EvidenceMapper mapper;

    @Test
    public void shouldMapEvidenceToCCD() {
        //given
        Evidence evidence = new Evidence(asList(new EvidenceRow(CORRESPONDENCE, "description")));

        //when
        CCDEvidence ccdEvidence = mapper.to(evidence);

        //then
        assertThat(evidence).isEqualTo(ccdEvidence);
    }

    @Test
    public void shouldMapEvidenceFromCCD() {
        //given
        CCDEvidenceRow evidenceRow = CCDEvidenceRow.builder()
            .description("My description")
            .type(CCDEvidenceType.EXPERT_WITNESS)
            .build();

        CCDEvidence ccdEvidence = CCDEvidence.builder()
            .rows(asList(CCDCollectionElement.<CCDEvidenceRow>builder().value(evidenceRow).build()))
            .build();

        //when
        Evidence evidence = mapper.from(ccdEvidence);

        //then
        assertThat(evidence).isEqualTo(ccdEvidence);
    }
}
