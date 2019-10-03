package uk.gov.hmcts.cmc.claimstore.services.ccd;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    CITIZEN("citizen"),
    CASEWORKER("caseworker-cmc"),
    SOLICITOR("caseworker-cmc-solicitor"),
    LEGAL_ADVISOR("caseworker-cmc-legaladvisor"),
    JUDGE("caseworker-cmc-judge");

    private String role;
}
