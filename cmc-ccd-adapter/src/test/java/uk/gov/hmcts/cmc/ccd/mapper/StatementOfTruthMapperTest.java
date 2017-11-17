package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import static org.assertj.core.api.Assertions.assertThat;

public class StatementOfTruthMapperTest {

    private StatementOfTruthMapper statementOfTruthMapper = new StatementOfTruthMapper();

    @Test
    public void shouldMapStatementOfTruthToCCD() {
        //given
        StatementOfTruth statementOfTruth = new StatementOfTruth("name", "role");

        //when
        uk.gov.hmcts.cmc.ccd.domain.StatementOfTruth ccdStatementOfTruth = statementOfTruthMapper.to(statementOfTruth);
        //then
        assertThat(ccdStatementOfTruth).isNotNull();
        assertThat(ccdStatementOfTruth.getSignerName()).isEqualTo("name");
        assertThat(ccdStatementOfTruth.getSignerRole()).isEqualTo("role");
    }

    @Test
    public void shouldMapStatementOfTruthFromCCD() {
        //given
        uk.gov.hmcts.cmc.ccd.domain.StatementOfTruth statementOfTruth = uk.gov.hmcts.cmc.ccd.domain.StatementOfTruth
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
