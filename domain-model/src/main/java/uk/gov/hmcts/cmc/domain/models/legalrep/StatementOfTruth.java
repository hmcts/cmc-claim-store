package uk.gov.hmcts.cmc.domain.models.legalrep;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.EqualsAndHashCode;

@Builder
@EqualsAndHashCode
public class StatementOfTruth {

    @NotBlank
    @Size(max = 70, message = "must be at most {max} characters")
    private final String signerName;

    @NotBlank
    @Size(max = 255, message = "must be at most {max} characters")
    private final String signerRole;

    public StatementOfTruth(String signerName, String signerRole) {
        this.signerName = signerName;
        this.signerRole = signerRole;
    }

    public String getSignerName() {
        return signerName;
    }

    public String getSignerRole() {
        return signerRole;
    }

}
