package uk.gov.hmcts.cmc.domain.models.legalrep;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Size;

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
