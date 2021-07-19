package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Fees {
    /**
     * The amount which was paid, in pennies for payments v1 or pounds with payments v2.
     */
    private final Integer id;
    private final String code;
    private final String version;
    private final Integer volume;
    private final Integer calculatedAmount;
    private final String ccdCaseNumber;
    private final String reference;

    public Fees(Integer id, String code, String version,
                Integer volume, Integer calculatedAmount,
                String ccdCaseNumber, String reference) {
        this.id = id;
        this.code = code;
        this.version = version;
        this.volume = volume;
        this.calculatedAmount = calculatedAmount;
        this.ccdCaseNumber = ccdCaseNumber;
        this.reference = reference;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
