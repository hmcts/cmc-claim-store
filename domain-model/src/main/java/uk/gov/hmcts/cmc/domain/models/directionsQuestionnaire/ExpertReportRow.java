package uk.gov.hmcts.cmc.domain.models.directionsQuestionnaire;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class ExpertReportRow {
    private String expertName;
    private LocalDate expertReportDate;
}
