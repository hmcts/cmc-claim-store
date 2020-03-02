package uk.gov.hmcts.cmc.claimstore;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.DirectionOrderService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.PilotCourtService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DirectionOrderServiceTest {

    @Mock
    private PilotCourtService pilotCourtService;

    private DirectionOrderService directionOrderService;

    private CCDAddress address;

    private final String courtName = "Birmingham Court";

    private HearingCourt hearingCourt;

    @BeforeEach
    void setUp() {
        directionOrderService = new DirectionOrderService(pilotCourtService);

        address = CCDAddress.builder()
            .addressLine1("line1")
            .addressLine2("line2")
            .addressLine3("line3")
            .postCode("SW1P4BB")
            .postTown("Birmingham")
            .build();

        hearingCourt = HearingCourt.builder().name(courtName).address(address).build();

    }

    @Test
    void shouldSetHearingCourtWhenPilotCourtSelected() {
        String pilotCourtName = "BIRMINGHAM";
        CCDCase ccdCase = CCDCase.builder()
            .hearingCourt(pilotCourtName)
            .build();

        when(pilotCourtService.getPilotHearingCourt(eq(pilotCourtName)))
            .thenReturn(Optional.of(HearingCourt.builder()
                .name(courtName)
                .address(address)
                .build()));

        HearingCourt returnedCourt = directionOrderService.getHearingCourt(ccdCase);

        Assertions.assertEquals(hearingCourt, returnedCourt);
    }

    @Test
    void shouldSetHearingCourtWhenOtherPilotCourtSelected() {

        String otherCourtName = "OTHER";
        CCDCase ccdCase = CCDCase.builder()
            .hearingCourt(otherCourtName)
            .hearingCourtName(courtName)
            .hearingCourtAddress(address)
            .build();

        HearingCourt returnedCourt = directionOrderService.getHearingCourt(ccdCase);

        Assertions.assertEquals(hearingCourt, returnedCourt);
    }

    @Test
    void shouldSetHearingCourtWhenNoPilotCourtSelected() {
        CCDCase ccdCase = CCDCase.builder()
            .hearingCourtName(courtName)
            .hearingCourtAddress(address)
            .build();
        HearingCourt returnedCourt = directionOrderService.getHearingCourt(ccdCase);

        Assertions.assertEquals(hearingCourt, returnedCourt);
    }
}
