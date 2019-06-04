package uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.CollectionId;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Getter
public class CCDExpertReportRow extends CollectionId {
    private String expertName;
    private LocalDate expertReportDate;

    @Builder
    public CCDExpertReportRow(String id, String expertName, LocalDate expertReportDate) {
        super(id);
        this.expertName = expertName;
        this.expertReportDate = expertReportDate;
    }
}
