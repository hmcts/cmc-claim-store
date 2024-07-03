package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@ExtendWith(SpringExtension.class)
public class StatementOfTruthCaseMapperTest {

    @Autowired
    private StatementOfTruthCaseMapper mapper;

    @Test
    public void shouldMapStatementOfTruthToCCD() {
        //given
        StatementOfTruth statementOfTruth = StatementOfTruth.builder().signerRole("Role")
            .signerName("Name").build();
        CCDCase.CCDCaseBuilder caseBuilder = CCDCase.builder();

        //when
        mapper.to(statementOfTruth, caseBuilder);
        CCDCase ccdCase = caseBuilder.build();

        //then
        assertEquals(ccdCase.getSotSignerName(), statementOfTruth.getSignerName());
        assertEquals(ccdCase.getSotSignerRole(), statementOfTruth.getSignerRole());
    }

    @Test
    public void shouldMapStatementOfTruthToCCDWithNullValues() {
        //given
        StatementOfTruth statementOfTruth = StatementOfTruth.builder().signerRole(null)
            .signerName(null).build();
        CCDCase.CCDCaseBuilder caseBuilder = CCDCase.builder();

        //when
        mapper.to(statementOfTruth, caseBuilder);
        CCDCase ccdCase = caseBuilder.build();

        //then
        assertEquals(ccdCase.getSotSignerName(), statementOfTruth.getSignerName());
        assertEquals(ccdCase.getSotSignerRole(), statementOfTruth.getSignerRole());
    }

    @Test
    public void shouldMapStatementOfTruthFromCCD() {
        //given
        CCDCase ccdCase = CCDCase.builder().sotSignerName("signerName")
            .sotSignerRole("signerRole").build();

        //when
        StatementOfTruth statementOfTruth = mapper.from(ccdCase);

        //then
        assertEquals(statementOfTruth.getSignerName(), ccdCase.getSotSignerName());
        assertEquals(statementOfTruth.getSignerRole(), ccdCase.getSotSignerRole());
    }

    @Test
    public void shouldMapEmptyCCDStatementOfTruthToNull() {
        //given
        CCDCase ccdCase = CCDCase.builder().build();

        //when
        StatementOfTruth statementOfTruth = mapper.from(ccdCase);

        //then
        assertNull(statementOfTruth);
    }
}
