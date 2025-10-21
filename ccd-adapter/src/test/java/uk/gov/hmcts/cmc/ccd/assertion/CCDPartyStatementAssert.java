package uk.gov.hmcts.cmc.ccd.assertion;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDPartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class CCDPartyStatementAssert extends CustomAssert<CCDPartyStatementAssert, CCDPartyStatement> {

    CCDPartyStatementAssert(CCDPartyStatement actual) {
        super("CCDPartyStatement", actual, CCDPartyStatementAssert.class);
    }

    public CCDPartyStatementAssert isEqualTo(PartyStatement expected) {
        isNotNull();

        compare("madeBy",
            expected.getMadeBy(), Enum::name,
            Optional.ofNullable(actual.getMadeBy()).map(Enum::name));

        compare("type",
            expected.getType(), Enum::name,
            Optional.ofNullable(actual.getType()).map(Enum::name));

        if (!actual.hasOffer()) {
            if (expected.getOffer().isPresent()) {
                failExpectedPresent("offer", expected.getOffer().get());
            }
            return this;
        }

        if (!expected.getOffer().isPresent()) {
            failExpectedAbsent("offer", ImmutableMap.of(
                "content", actual.getOfferContent(),
                "completionDate", actual.getOfferCompletionDate(),
                "paymentIntention", actual.getPaymentIntention()
            ));
        }

        compare("offerContent",
            expected.getOffer().map(Offer::getContent).orElse(null),
            Optional.ofNullable(actual.getOfferContent()));

        compare("offerCompletionDate",
            expected.getOffer().map(Offer::getCompletionDate).orElse(null),
            Optional.ofNullable(actual.getOfferCompletionDate()));

        compare("paymentIntention",
            expected.getOffer().flatMap(Offer::getPaymentIntention).orElse(null),
            Optional.ofNullable(actual.getPaymentIntention()),
            (e, a) -> assertThat(e).isEqualTo(a));

        return this;
    }
}
