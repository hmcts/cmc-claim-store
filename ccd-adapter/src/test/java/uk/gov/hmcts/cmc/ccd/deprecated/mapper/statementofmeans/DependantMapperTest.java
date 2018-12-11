package uk.gov.hmcts.cmc.ccd.deprecated.mapper.statementofmeans;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDChild;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDDependant;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDOtherDependants;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Dependant;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.OtherDependants;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Child.AgeGroupType.BETWEEN_11_AND_15;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Child.AgeGroupType.BETWEEN_16_AND_19;

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
            .children(asList(Child.builder()
                .numberOfChildren(3)
                .ageGroupType(BETWEEN_16_AND_19)
                .numberOfChildrenLivingWithYou(1)
                .build())
            )
            .numberOfMaintainedChildren(1)
            .otherDependants(OtherDependants.builder().numberOfPeople(1).details("Father").build())
            .build();

        //when
        CCDDependant ccdDependant = mapper.to(dependant);

        //then
        assertThat(dependant).isEqualTo(ccdDependant);

    }

    @Test
    public void shouldMapDependantFromCCD() {
        //given
        CCDChild ccdChild = CCDChild.builder()
            .numberOfChildren(4)
            .numberOfChildrenLivingWithYou(1)
            .ageGroupType(BETWEEN_11_AND_15)
            .build();

        CCDDependant ccdDependant = CCDDependant.builder()
            .children(asList(CCDCollectionElement.<CCDChild>builder().value(ccdChild).build()))
            .numberOfMaintainedChildren(2)
            .otherDependants(CCDOtherDependants.builder()
                .numberOfPeople(2)
                .details("Parents")
                .build()
            )
            .build();
        //when
        Dependant dependant = mapper.from(ccdDependant);

        //then
        assertThat(dependant).isEqualTo(ccdDependant);
    }
}
