package uk.gov.hmcts.cmc.domain.models.legalrep;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@Getter
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

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
