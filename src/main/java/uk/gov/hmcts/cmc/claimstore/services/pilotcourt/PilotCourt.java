package uk.gov.hmcts.cmc.claimstore.services.pilotcourt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
public class PilotCourt {
    private String id;
    private String postcode;
    private HearingCourt hearingCourt;
    private Map<Pilot, LocalDateTime> pilots;

    public Optional<HearingCourt> getHearingCourt() {
        return Optional.ofNullable(hearingCourt);
    }

    public boolean isActivePilotCourt(Pilot pilot, LocalDateTime claimCreatedDate) {
        return pilots.containsKey(pilot) && !pilots.get(pilot).isAfter(claimCreatedDate);
    }
}
