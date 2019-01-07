package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class StatementOfTruthCaseMapperTest {

    @Autowired
    private StatementOfTruthCaseMapper mapper;

    @Test
    public void mapStatementOfTruthToCCD() {
        //given
        StatementOfTruth statementOfTruth = StatementOfTruth.builder().signerRole("Role")
            .signerName("Name").build();
        CCDCase.CCDCaseBuilder caseBuilder = CCDCase.builder();


        //when
        mapper.to(statementOfTruth, caseBuilder);
        CCDCase ccdCase = caseBuilder.build();

        //then
        Assert.assertEquals(ccdCase.getSotSignerName(), statementOfTruth.getSignerName());
        Assert.assertEquals(ccdCase.getSotSignerRole(), statementOfTruth.getSignerRole());

    }

    @Test
    public void mapCCDStatementOfTruthToDomain() {
        //given
        CCDCase ccdCase = CCDCase.builder().sotSignerName("signerName")
            .sotSignerRole("signerRole").build();


        //when
        StatementOfTruth statementOfTruth = mapper.from(ccdCase);

        //then
        Assert.assertEquals(statementOfTruth.getSignerName(), ccdCase.getSotSignerName());
        Assert.assertEquals(statementOfTruth.getSignerRole(), ccdCase.getSotSignerRole());

    }


}
