package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import org.junit.Test;

import java.util.Arrays;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PilotCourtTest {

    @Test
    public void shouldReturnTrueForPilotCourt() {
        Arrays.stream(PilotCourt.values())
            .filter(pilotCourt -> pilotCourt != PilotCourt.OTHER)
            .forEach(
                court -> assertTrue(PilotCourt.isPilotCourt(court.getName() + randomAlphabetic(15)))
            );
    }

    @Test
    public void shouldReturnFalseForNonPilotCourt() {
        assertFalse(PilotCourt.isPilotCourt("London Court"));
    }

    @Test
    public void shouldReturnFalseForNullCourtName() {
        assertFalse(PilotCourt.isPilotCourt(null));
    }

    @Test
    public void shouldReturnFalseForEMptyCourtName() {
        assertFalse(PilotCourt.isPilotCourt(""));
    }
}
