package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDStatementOfMeans;
import uk.gov.hmcts.cmc.domain.models.sampledata.statementofmeans.SampleStatementOfMeans;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDStatementOfMeans;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class StatementOfMeansMapperTest {

    @Autowired
    private StatementOfMeansMapper mapper;

    @Test
    public void shouldMapStatementOfMeansToCCD() {
        //given
        StatementOfMeans statementOfMeans = SampleStatementOfMeans.builder().build();
        //when
        CCDStatementOfMeans ccdStatementOfMeans = mapper.to(statementOfMeans);

        //then
        assertThat(statementOfMeans).isEqualTo(ccdStatementOfMeans);
    }

    @Test
    public void shouldMapStatementOfMeansFromCCD() {
        //given
        CCDStatementOfMeans ccdStatementOfMeans = getCCDStatementOfMeans();

        //when
        StatementOfMeans statementOfMeans = mapper.from(ccdStatementOfMeans);

        //then
        assertThat(statementOfMeans).isEqualTo(ccdStatementOfMeans);
    }
}
