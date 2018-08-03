package uk.gov.hmcts.cmc.ccd.domain.offers;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class CCDOffer {
    private String content;
    private LocalDate completionDate;
    private boolean generated;
}
