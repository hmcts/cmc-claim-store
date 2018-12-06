package uk.gov.hmcts.cmc.domain.models.evidence;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
public class Evidence {

    @Valid
    @Size(max = 1000)
    private final List<EvidenceRow> rows;

    public Evidence(@JsonProperty("rows") List<EvidenceRow> rows) {
        this.rows = rows;
    }

    public List<EvidenceRow> getRows() {
        return rows;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
