package uk.gov.hmcts.cmc.ccd.deprecated.mapper.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDChild;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Child.AgeGroupType.BETWEEN_11_AND_15;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Child.AgeGroupType.BETWEEN_16_AND_19;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ChildMapperTest {

    @Autowired
    private ChildMapper mapper;

    @Test
    public void shouldMapChildToCCD() {
        //given
        Child child = Child.builder()
            .numberOfChildren(3)
            .ageGroupType(BETWEEN_16_AND_19)
            .numberOfChildrenLivingWithYou(1)
            .build();

        //when
        CCDChild ccdChild = mapper.to(child);

        //then
        assertThat(child).isEqualTo(ccdChild);

    }

    @Test
    public void shouldMapChildFromCCD() {
        //given
        CCDChild ccdChild = CCDChild.builder()
            .numberOfChildren(4)
            .numberOfChildrenLivingWithYou(1)
            .ageGroupType(BETWEEN_11_AND_15)
            .build();

        //when
        Child child = mapper.from(ccdChild);

        //then
        assertThat(child).isEqualTo(ccdChild);
    }
}
