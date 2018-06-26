package uk.gov.hmcts.cmc.claimstore.repositories.mapping;

import org.junit.Ignore;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.repositories.mapping.MappingUtils.toLocalDateTimeFromUTC;
import static uk.gov.hmcts.cmc.claimstore.repositories.mapping.MappingUtils.toNullableLocalDateTimeFromUTC;
import static uk.gov.hmcts.cmc.claimstore.repositories.mapping.MappingUtils.toNullableLong;

public class MappingUtilsTest {

    @Ignore
    @Test
    public void toNullableLongShouldReturnLongPrimitiveType() {
        assertThat(toNullableLong(null)).isNull();
        assertThat(toNullableLong(100)).isEqualTo(100L);
        assertThat(toNullableLong(0)).isEqualTo(0L);
        assertThat(toNullableLong(-100)).isEqualTo(-100L);
    }

    @Ignore
    @Test
    public void toNullableLongShouldReturnNull() {
        assertThat(toNullableLong(null)).isNull();
    }

    @Ignore
    @Test
    public void toNullableLocalDateTimeFromUTCShouldReturnNull() {
        assertThat(toNullableLocalDateTimeFromUTC(null)).isNull();
    }

    @Ignore
    @Test(expected = NullPointerException.class)
    public void toLocalDateTimeFromUtcWhenNullShouldThrow() {
        toLocalDateTimeFromUTC(null);
    }

    @Ignore
    @Test
    public void toLocalDateTimeFromUtcReturnsLocalDateTime() {
        LocalDateTime dateTime = ZonedDateTime.of(2016, 12, 12, 10, 10, 0, 0, ZoneId.of("UTC")).toLocalDateTime();
        assertThat(toLocalDateTimeFromUTC(new Timestamp(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli())))
            .isEqualTo(dateTime);
    }
}
