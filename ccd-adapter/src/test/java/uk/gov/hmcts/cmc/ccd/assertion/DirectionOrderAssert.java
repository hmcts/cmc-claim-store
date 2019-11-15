package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.domain.models.orders.DirectionOrder;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class DirectionOrderAssert extends CustomAssert<DirectionOrderAssert, DirectionOrder> {

    DirectionOrderAssert(DirectionOrder actual) {
        super("DirectionOrder", actual, DirectionOrderAssert.class);
    }

    public DirectionOrderAssert isEqualTo(CCDDirectionOrder expected) {
        isNotNull();

        compare("createdOn",
            expected.getCreatedOn(),
            Optional.ofNullable(actual.getCreatedOn()));

        compare("hearingCourtAddress",
            expected.getHearingCourtAddress(),
            Optional.ofNullable(actual.getHearingCourtAddress()),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("hearingCourtName",
            expected.getHearingCourtName(),
            Optional.ofNullable(actual.getHearingCourtName()));

        return this;
    }

}
