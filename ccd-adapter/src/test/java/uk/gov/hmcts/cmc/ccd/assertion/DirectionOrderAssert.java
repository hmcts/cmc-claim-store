package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.domain.orders.DirectionOrder;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class DirectionOrderAssert extends AbstractAssert<DirectionOrderAssert, DirectionOrder> {

    public DirectionOrderAssert(DirectionOrder actual) {
        super(actual, DirectionOrderAssert.class);
    }

    public DirectionOrderAssert isEqualTo(CCDDirectionOrder ccdDirectionOrder) {
        isNotNull();

        Optional.ofNullable(ccdDirectionOrder.getCreatedOn()).ifPresent(isCreatedOnEqual());

        assertThat(actual.getHearingCourtAddress()).isEqualTo(ccdDirectionOrder.getHearingCourtAddress());

        return this;
    }

    private Consumer<LocalDateTime> isCreatedOnEqual() {
        return createdOn -> {
            if (!Objects.equals(actual.getCreatedOn(), createdOn)) {
                failWithMessage("Expected DirectionOrder.createdOn to be <%s> but was <%s>",
                    createdOn, actual.getCreatedOn());
            }
        };
    }

}
