package uk.gov.hmcts.cmc.ccd_adapter.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDPartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;

import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.cmc.ccd_adapter.assertion.Assertions.assertThat;

public class PartyStatementAssert extends AbstractAssert<PartyStatementAssert, PartyStatement> {

    public PartyStatementAssert(PartyStatement actual) {
        super(actual, PartyStatementAssert.class);
    }

    public PartyStatementAssert isEqualTo(CCDPartyStatement ccdPartyStatement) {
        isNotNull();

        if (null != actual.getMadeBy()
            && !Objects.equals(actual.getMadeBy().name(), ccdPartyStatement.getMadeBy().name())) {
            failWithMessage("Expected Party Statement.made by to be <%s> but was <%s>",
                ccdPartyStatement.getMadeBy(), actual.getMadeBy().name());
        }

        if (null != actual.getType()
            && !Objects.equals(actual.getType().name(), ccdPartyStatement.getType().name())) {
            failWithMessage("Expected Party Statement.type to be <%s> but was <%s>",
                ccdPartyStatement.getType().name(), actual.getType().name());
        }

        actual.getOffer().ifPresent(offer -> {
                assertEquals(offer.getContent(), ccdPartyStatement.getOfferContent());
                assertEquals(offer.getCompletionDate(), ccdPartyStatement.getOfferCompletionDate());

                offer.getPaymentIntention().ifPresent(
                    paymentIntention -> assertThat(paymentIntention).isEqualTo(ccdPartyStatement.getPaymentIntention())
                );
            }
        );

        return this;
    }
}
