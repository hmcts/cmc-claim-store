package uk.gov.hmcts.cmc.ccd.adapter.mapper.defendant.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.adapter.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDChildCategory;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.adapter.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDAgeGroupType.BETWEEN_11_AND_15;
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
        CCDCollectionElement<CCDChildCategory> ccdChildCategory = mapper.to(child);

        //then
        assertThat(child).isEqualTo(ccdChildCategory.getValue());
        assertThat(child.getId()).isEqualTo(ccdChildCategory.getId());
    }

    @Test
    public void shouldMapChildFromCCD() {
        //given
        CCDChildCategory ccdChildCategory = CCDChildCategory.builder()
            .numberOfChildren(4)
            .numberOfResidentChildren(1)
            .ageGroupType(BETWEEN_11_AND_15)
            .build();
        String collectionId = UUID.randomUUID().toString();

        //when
        Child child = mapper.from(CCDCollectionElement.<CCDChildCategory>builder()
            .id(collectionId)
            .value(ccdChildCategory)
            .build());

        //then
        assertThat(child).isEqualTo(ccdChildCategory);
        assertThat(child.getId()).isEqualTo(collectionId);
    }
}
