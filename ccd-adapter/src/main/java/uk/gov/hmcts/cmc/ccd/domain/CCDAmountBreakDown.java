package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CCDAmountBreakDown {
    private List<CCDCollectionElement<CCDAmountRow>> rows;

    @JsonCreator
    public CCDAmountBreakDown(List<CCDCollectionElement<CCDAmountRow>> rows) {
        this.rows = rows;
    }
}
