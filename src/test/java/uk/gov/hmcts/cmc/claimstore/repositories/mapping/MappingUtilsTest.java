package uk.gov.hmcts.cmc.claimstore.repositories.mapping;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.repositories.mapping.MappingUtils.toNullableLocalDateTimeFromUTC;
import static uk.gov.hmcts.cmc.claimstore.repositories.mapping.MappingUtils.toNullableLong;

public class MappingUtilsTest {

    @Test
    public void toNullableLongShouldReturnLongPrimitiveType() {

        assertThat(toNullableLong(null)).isNull();
        assertThat(toNullableLong(Integer.valueOf(100))).isEqualTo(100L);
        assertThat(toNullableLong(Integer.valueOf(0))).isEqualTo(0L);
        assertThat(toNullableLong(Integer.valueOf(-100))).isEqualTo(-100L);
    }

    @Test
    public void toNullableLongShouldReturnNull() {

        assertThat(toNullableLong(null)).isNull();
    }

    @Test
    public void toNullableLocalDateTimeFromUTCShouldReturnNull() {

        assertThat(toNullableLocalDateTimeFromUTC(null)).isNull();
    }
}
