package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDChildren;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDDependant;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Children;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Dependant;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DependantMapperTest {

    @Autowired
    private DependantMapper mapper;

    @Test
    public void shouldMapDependantToCCD() {
        //given
        Dependant dependant = Dependant.builder()
            .children(Children.builder().between11and15(0).between16and19(1).between16and19(2).build())
            .maintainedChildren(1)
            .build();

        //when
        CCDDependant ccdDependant = mapper.to(dependant);

        //then
        assertThat(dependant).isEqualTo(ccdDependant);

    }

    @Test
    public void shouldMapDependantFromCCD() {
        //given
        CCDDependant ccdDependant = CCDDependant.builder()
            .maintainedChildren(1)
            .children(CCDChildren.builder().between16and19(2).between11and15(1).under11(0).build())
            .build();

        //when
        Dependant dependant = mapper.from(ccdDependant);

        //then
        assertThat(dependant).isEqualTo(ccdDependant);
    }
}
