package uk.gov.hmcts.cmc.claimstore.repositories.mapping;

import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.repositories.mapping.MappingUtils.toLocalDateTimeFromUTC;
import static uk.gov.hmcts.cmc.claimstore.repositories.mapping.MappingUtils.toNullableLocalDateFromUTC;
import static uk.gov.hmcts.cmc.claimstore.repositories.mapping.MappingUtils.toNullableLocalDateTimeFromUTC;
import static uk.gov.hmcts.cmc.claimstore.repositories.mapping.MappingUtils.toNullableLong;

public class MappingUtilsTest {

    @Test
    public void toNullableLongShouldReturnLongPrimitiveType() {
        assertThat(toNullableLong(100)).isEqualTo(100L);
        assertThat(toNullableLong(0)).isEqualTo(0L);
        assertThat(toNullableLong(-100)).isEqualTo(-100L);
    }

    @Test
    public void toNullableLongShouldReturnNull() {
        //noinspection ConstantConditions
        assertThat(toNullableLong(null)).isNull();
    }

    @Test
    public void toNullableLocalDateTimeFromUTCShouldReturnNull() {
        assertThat(toNullableLocalDateTimeFromUTC(null)).isNull();
    }

    @Test(expected = NullPointerException.class)
    public void toLocalDateTimeFromUtcWhenNullShouldThrow() {
        //noinspection ConstantConditions
        toLocalDateTimeFromUTC(null);
    }

    @Test
    public void toNullableLocalDateFromUTCShouldReturnNull() {
        assertThat(toNullableLocalDateFromUTC(null)).isNull();
    }

    @Test
    public void toNullableLocalDateFromUTCShouldReturnLocalDate() {
        assertThat(toNullableLocalDateFromUTC(Timestamp.valueOf("2010-10-10 10:10:10"))).isInstanceOf(LocalDate.class);
    }

    @Test
    public void toLocalDateTimeFromUtcReturnsLocalDateTime() {
        LocalDateTime dateTime = ZonedDateTime.of(2016, 12, 12, 10, 10, 0, 0, ZoneId.of("UTC")).toLocalDateTime();
        assertThat(toLocalDateTimeFromUTC(new Timestamp(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli())))
            .isEqualTo(dateTime);
    }
}
