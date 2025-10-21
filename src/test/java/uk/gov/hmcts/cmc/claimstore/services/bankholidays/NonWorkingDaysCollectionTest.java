package uk.gov.hmcts.cmc.claimstore.services.bankholidays;

import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

public class NonWorkingDaysCollectionTest {

    private NonWorkingDaysCollection collection;

    @Test
    public void testWithMatchingNonWorkingDays() {
        collection = new NonWorkingDaysCollection("/non-working-days/nwd-valid-includes-2019-02-02.dat");
        assertThat(collection.contains(LocalDate.of(2019, Month.FEBRUARY, 2))).isTrue();
    }

    @Test
    public void testWithNoNonWorkingDays() {
        collection = new NonWorkingDaysCollection("/non-working-days/nwd-empty-file.dat");
        assertThat(collection.contains(LocalDate.now())).isFalse();
    }

    @Test
    public void testWithNonMatchingNonWorkingDays() {
        collection = new NonWorkingDaysCollection("/non-working-days/nwd-valid-excludes-2019-01-03.dat");
        assertThat(collection.contains(LocalDate.of(2019, Month.JANUARY, 3))).isFalse();
    }

    @Test
    public void testWithIncoherentNonWorkingDays() {
        collection = new NonWorkingDaysCollection("/non-working-days/nwd-invalid.dat");
        assertThat(collection.contains(LocalDate.now())).isFalse();
    }
}
