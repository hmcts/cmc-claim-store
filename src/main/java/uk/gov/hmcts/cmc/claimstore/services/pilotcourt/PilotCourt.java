package uk.gov.hmcts.cmc.claimstore.services.pilotcourt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public Optional<HearingCourt> getHearingCourt() {
        return Optional.ofNullable(hearingCourt);
    }

    public boolean isActivePilotCourt(Pilot pilot, LocalDateTime claimCreatedDate) {
        if (pilots.get(pilot).isAfter(claimCreatedDate)) {
            logger.info("Pilot court is online after the claim created date");
        }
        return pilots.containsKey(pilot);
    }
}
