package uk.gov.hmcts.cmc.claimstore.services.pilotcourt;

import java.util.Optional;

public enum PilotCourtCSVHeader {
    //Order of enum elements relate to the order of the csv headers
    ID,
    POSTCODE,
    LA_PILOT(Pilot.LA),
    JDDO_PILOT(Pilot.JDDO);

    private Pilot pilot;

    PilotCourtCSVHeader() {

    }

    PilotCourtCSVHeader(Pilot pilot) {
        this.pilot = pilot;
    }

    public Optional<Pilot> getPilot() {
        return Optional.ofNullable(pilot);
    }

}
