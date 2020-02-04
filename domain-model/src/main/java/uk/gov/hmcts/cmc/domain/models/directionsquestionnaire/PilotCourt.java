package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum PilotCourt {
    EDMONTON("edmonton"),
    MANCHESTER("manchester"),
    BIRMINGHAM("birmingham"),
    CLERKENWELL("clerkenwell"),
    SHOREDITCH("shoreditch"),
    OTHER("");

    private String name;

    public String getName() {
        return name;
    }

    public static boolean isPilotCourt(String courtName) {
        if (courtName == null) {
            return false;
        }
        return Arrays.stream(PilotCourt.values())
            .filter(pilotCourt -> pilotCourt != PilotCourt.OTHER)
            .anyMatch(pilotCourt -> courtName.toLowerCase().contains(pilotCourt.name));
    }
}
