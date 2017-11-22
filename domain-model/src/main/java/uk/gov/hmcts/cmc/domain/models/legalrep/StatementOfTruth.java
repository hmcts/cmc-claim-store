package uk.gov.hmcts.cmc.domain.models.legalrep;

import org.hibernate.validator.constraints.NotBlank;

import java.util.Objects;
import javax.validation.constraints.Size;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        StatementOfTruth that = (StatementOfTruth) obj;

        return Objects.equals(this.signerName, that.signerName) && Objects.equals(this.signerRole, that.signerRole);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signerName, signerRole);
    }

}
