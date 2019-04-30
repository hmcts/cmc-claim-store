package uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Judicial {
    private String firstName;

    private String lastName;
}
