package uk.gov.hmcts.cmc.domain.models.evidence;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.List;
import java.util.Optional;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode(callSuper = true)
public class DefendantEvidence extends Evidence {

    @Size(max = 99000)
    private final String comment;

    public DefendantEvidence(List<EvidenceRow> rows, String comment) {
        super(rows);
        this.comment = comment;
    }

    public Optional<String> getComment() {
        return Optional.ofNullable(comment);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }

}
