package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.orders.DirectionOrder;

import java.time.LocalDateTime;

public class SampleDirectionOrder {

    private SampleDirectionOrder() {
    }

    public static DirectionOrder.DirectionOrderBuilder builder() {
        return DirectionOrder.builder()
            .createdOn(LocalDateTime.now())
            .hearingCourtAddress(SampleAddress.builder().build());
    }

    public static DirectionOrder getDefault() {
        return builder().build();
    }
}
