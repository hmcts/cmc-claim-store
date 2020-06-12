package uk.gov.hmcts.cmc.domain.models.bulkprint;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class BulkPrintDetails {
    private String printLetterId;
    private PrintLetterType printLetterType;
    private LocalDate printedDate;
}
