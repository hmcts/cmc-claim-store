package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDStatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class StatementOfTruthMapperTest {

    @Autowired
    private StatementOfTruthMapper statementOfTruthMapper;

    @Test
    public void shouldMapStatementOfTruthToCCD() {
        //given
        StatementOfTruth statementOfTruth = new StatementOfTruth("name", "role");

        //when
        CCDStatementOfTruth ccdStatementOfTruth = statementOfTruthMapper.to(statementOfTruth);
        //then
        assertThat(ccdStatementOfTruth).isNotNull();
        assertThat(ccdStatementOfTruth.getSignerName()).isEqualTo("name");
        assertThat(ccdStatementOfTruth.getSignerRole()).isEqualTo("role");
    }

    @Test
    public void shouldMapStatementOfTruthFromCCD() {
        //given
        CCDStatementOfTruth statementOfTruth = CCDStatementOfTruth
            .builder()
            .signerName("name")
            .signerRole("role")
            .build();

        //when
        StatementOfTruth cmcStatementOfTruth = statementOfTruthMapper.from(statementOfTruth);
        //then
        assertThat(cmcStatementOfTruth).isNotNull();
        assertThat(cmcStatementOfTruth.getSignerName()).isEqualTo(statementOfTruth.getSignerName());
        assertThat(cmcStatementOfTruth.getSignerRole()).isEqualTo(statementOfTruth.getSignerRole());
    }
}
