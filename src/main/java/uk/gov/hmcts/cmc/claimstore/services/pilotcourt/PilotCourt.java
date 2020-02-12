package uk.gov.hmcts.cmc.claimstore.services.pilotcourt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
public class PilotCourt {
    private String id;
    private String postcode;
    private HearingCourt hearingCourt;

    public Optional<HearingCourt> getHearingCourt() {
        return Optional.ofNullable(hearingCourt);
    }
}
