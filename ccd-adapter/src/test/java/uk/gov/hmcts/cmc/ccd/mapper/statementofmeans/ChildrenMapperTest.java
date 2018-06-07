package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDChildren;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Children;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ChildrenMapperTest {

    @Autowired
    private ChildrenMapper mapper;

    @Test
    public void shouldMapChildrenToCCD() {
        //given
        Children children = Children.builder()
            .under11(1)
            .between11and15(2)
            .between16and19(3)
            .build();

        //when
        CCDChildren ccdChildren = mapper.to(children);

        //then
        assertThat(children).isEqualTo(ccdChildren);

    }

    @Test
    public void shouldMapChildrenFromCCD() {
        //given
        CCDChildren ccdChildren = CCDChildren.builder()
            .between16and19(2)
            .between11and15(1)
            .under11(0)
            .build();

        //when
        Children children = mapper.from(ccdChildren);

        //then
        assertThat(children).isEqualTo(ccdChildren);
    }
}
