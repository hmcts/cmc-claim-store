package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.PilotCourt.MANCHESTER;

public class PilotCourtTest {

    @Test
    public void shouldReturnTrueForPilotCourt() {
        assertTrue(PilotCourt.isPilotCourt(MANCHESTER.getName()));
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
