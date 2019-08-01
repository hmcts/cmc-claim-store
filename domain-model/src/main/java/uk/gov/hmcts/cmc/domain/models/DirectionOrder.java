package uk.gov.hmcts.cmc.domain.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@EqualsAndHashCode
@Getter
public class DirectionOrder {
    private LocalDateTime createdOn;
    private Address hearingCourtAddress;
}
