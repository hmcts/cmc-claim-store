package uk.gov.hmcts.cmc.ccd.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;

public class CCDYesNoOptionTest {

    @Test
    public void shouldConvertYesToTrue() {
        assertThat(YES.toBoolean()).isTrue();
    }

    @Test
    public void shouldConvertNoToFalse() {
        assertThat(NO.toBoolean()).isFalse();
    }

    @Test
    public void shouldConvertFalseToNo() {
        assertThat(CCDYesNoOption.valueOf(false)).isEqualTo(NO);
    }

    @Test
    public void shouldConvertTrueToYes() {
        assertThat(CCDYesNoOption.valueOf(true)).isEqualTo(YES);
    }
}
