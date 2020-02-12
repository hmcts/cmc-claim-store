package uk.gov.hmcts.cmc.domain.models.bulkprint;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class BulkPrintDetails {
    private String bulkPrintLetterId;
    private BulkPrintLetterType bulkPrintLetterType;
}
