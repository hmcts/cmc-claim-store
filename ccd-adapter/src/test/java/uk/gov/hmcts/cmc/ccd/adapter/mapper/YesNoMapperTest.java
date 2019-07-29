package uk.gov.hmcts.cmc.ccd.adapter.mapper;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.adapter.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class YesNoMapperTest {

    @Autowired
    private YesNoMapper mapper;

    @Test
    public void shouldMapYesToCCD() {
        Assertions.assertThat(mapper.to(YesNoOption.YES)).isEqualTo(CCDYesNoOption.YES);
    }

    @Test
    public void shouldMapNoToCCD() {
        Assertions.assertThat(mapper.to(YesNoOption.NO)).isEqualTo(CCDYesNoOption.NO);
    }

    @Test
    public void shouldMapNullToCCD() {
        Assertions.assertThat(mapper.to(null)).isNull();
    }

    @Test
    public void shouldMapYesFromCCD() {
        Assertions.assertThat(mapper.from(CCDYesNoOption.YES)).isEqualTo(YesNoOption.YES);
    }

    @Test
    public void shouldMapNOFromCCD() {
        Assertions.assertThat(mapper.from(CCDYesNoOption.NO)).isEqualTo(YesNoOption.NO);
    }

    @Test
    public void shouldMapNullFromCCD() {
        Assertions.assertThat(mapper.from(null)).isNull();
    }
}
