package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDChildCategory;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Child.AgeGroupType.BETWEEN_11_AND_15;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Child.AgeGroupType.BETWEEN_16_AND_19;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ChildCategoryMapperTest {

    @Autowired
    private ChildCategoryMapper mapper;

    @Test
    public void shouldMapChildToCCD() {
        //given
        Child child = Child.builder()
            .numberOfChildren(3)
            .ageGroupType(BETWEEN_16_AND_19)
            .numberOfChildrenLivingWithYou(1)
            .build();

        //when
        CCDChildCategory ccdChildCategory = mapper.to(child);

        //then
        assertThat(child).isEqualTo(ccdChildCategory);

    }

    @Test
    public void shouldMapChildFromCCD() {
        //given
        CCDChildCategory ccdChildCategory = CCDChildCategory.builder()
            .numberOfChildren(4)
            .numberOfResidentChildren(1)
            .ageGroupType(BETWEEN_11_AND_15)
            .build();

        //when
        Child child = mapper.from(ccdChildCategory);

        //then
        assertThat(child).isEqualTo(ccdChildCategory);
    }
}
