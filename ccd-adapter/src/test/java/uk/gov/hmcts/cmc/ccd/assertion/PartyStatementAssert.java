package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDPartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class PartyStatementAssert extends CustomAssert<PartyStatementAssert, PartyStatement> {

    PartyStatementAssert(PartyStatement actual) {
        super("PartyStatement", actual, PartyStatementAssert.class);
    }

    public PartyStatementAssert isEqualTo(CCDPartyStatement expected) {
        isNotNull();

        compare("madeBy",
            expected.getMadeBy(), Enum::name,
            Optional.ofNullable(actual.getMadeBy()).map(Enum::name));

        compare("type",
            expected.getType(), Enum::name,
            Optional.ofNullable(actual.getType()).map(Enum::name));

        compare("offerContent",
            expected.getOfferContent(),
            actual.getOffer().map(Offer::getContent));

        compare("offerCompletionDate",
            expected.getOfferCompletionDate(),
            actual.getOffer().map(Offer::getCompletionDate));

        compare("paymentIntention",
            expected.getPaymentIntention(),
            actual.getOffer().flatMap(Offer::getPaymentIntention),
            (e, a) -> assertThat(a).isEqualTo(e));

        return this;
    }
}
