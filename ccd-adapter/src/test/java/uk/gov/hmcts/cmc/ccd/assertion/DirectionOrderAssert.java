package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.domain.models.DirectionOrder;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class DirectionOrderAssert extends AbstractAssert<DirectionOrderAssert, DirectionOrder> {

    public DirectionOrderAssert(DirectionOrder actual) {
        super(actual, DirectionOrderAssert.class);
    }

    public DirectionOrderAssert isEqualTo(CCDDirectionOrder ccdDirectionOrder) {
        isNotNull();

        if (!Objects.equals(actual.getCreatedOn(), ccdDirectionOrder.getCreatedOn())) {
            failWithMessage("Expected DirectionOrder.createdOn to be <%s> but was <%s>",
                ccdDirectionOrder.getCreatedOn(), actual.getCreatedOn());
        }

        assertThat(actual.getHearingCourtAddress()).isEqualTo(ccdDirectionOrder.getHearingCourtAddress());

        return this;
    }

}
