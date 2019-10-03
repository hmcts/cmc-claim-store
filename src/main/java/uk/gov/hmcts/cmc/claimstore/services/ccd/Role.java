package uk.gov.hmcts.cmc.claimstore.services.ccd;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Role {
    CITIZEN("citizen"),
    CASEWORKER("caseworker-cmc"),
    SOLICITOR("caseworker-cmc-solicitor"),
    LEGAL_ADVISOR("caseworker-cmc-legaladvisor"),
    JUDGE("caseworker-cmc-judge");

    private String role;

    public static Role fromValue(String value) {
        return Arrays.stream(Role.values())
            .filter(val -> val.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
