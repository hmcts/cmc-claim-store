package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.CollectionId;

import java.time.LocalDate;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ExpertReport extends CollectionId {
    private String expertName;
    private LocalDate expertReportDate;

    @Builder
    public ExpertReport(String id, String expertName, LocalDate expertReportDate) {
        super(id);
        this.expertName = expertName;
        this.expertReportDate = expertReportDate;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
