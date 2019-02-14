package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceRow;
import uk.gov.hmcts.cmc.ccd.domain.evidence.CCDEvidenceType;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.evidence.EvidenceType.CORRESPONDENCE;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class EvidenceRowMapperTest {

    @Autowired
    private EvidenceRowMapper mapper;

    @Test
    public void shouldMapEvidenceRowToCCD() {
        //given
        EvidenceRow evidenceRow = EvidenceRow.builder().type(CORRESPONDENCE).description("description").build();

        //when
        CCDCollectionElement<CCDEvidenceRow> ccdEvidenceRow = mapper.to(evidenceRow);

        //then
        assertThat(evidenceRow).isEqualTo(ccdEvidenceRow.getValue());
        assertThat(evidenceRow.getId()).isEqualTo(ccdEvidenceRow.getId());
    }

    @Test
    public void shouldMapEvidenceRowToCCDWhenNoDescriptionProvided() {
        //given
        EvidenceRow evidenceRow = EvidenceRow.builder().type(CORRESPONDENCE).description(null).build();

        //when
        CCDCollectionElement<CCDEvidenceRow> ccdEvidenceRow = mapper.to(evidenceRow);

        //then
        assertThat(evidenceRow).isEqualTo(ccdEvidenceRow.getValue());
        assertThat(evidenceRow.getId()).isEqualTo(ccdEvidenceRow.getId());
    }

    @Test
    public void shouldMapEvidenceRowFromCCD() {
        //given
        CCDEvidenceRow ccdEvidenceRow = CCDEvidenceRow.builder()
            .description("My description")
            .type(CCDEvidenceType.EXPERT_WITNESS)
            .build();

        //when
        EvidenceRow evidenceRow = mapper.from(CCDCollectionElement.<CCDEvidenceRow>builder()
            .value(ccdEvidenceRow).build());

        //then
        assertThat(evidenceRow).isEqualTo(ccdEvidenceRow);
    }

    @Test
    public void shouldMapEvidenceRowFromCCDWhenNoDescriptionProvided() {
        //given
        CCDEvidenceRow ccdEvidenceRow = CCDEvidenceRow.builder()
            .type(CCDEvidenceType.EXPERT_WITNESS)
            .build();

        //when
        EvidenceRow evidenceRow = mapper.from(CCDCollectionElement.<CCDEvidenceRow>builder()
            .value(ccdEvidenceRow).build());

        //then
        assertThat(evidenceRow).isEqualTo(ccdEvidenceRow);
    }
}
