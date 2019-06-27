package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum PilotCourt {
    EDMONTON("Birmingham"),
    MANCHESTER("Manchester"),
    BIRMINGHAM("Birmingham"),
    CLERKENWELL("Clerkenwell & Shoreditch");

    private String name;

    public String getName() {
        return name;
    }

    public static boolean isPilotCourt(String courtName) {
        return Arrays.stream(PilotCourt.values())
            .anyMatch(val -> val.name().equalsIgnoreCase(courtName));
    }
}
