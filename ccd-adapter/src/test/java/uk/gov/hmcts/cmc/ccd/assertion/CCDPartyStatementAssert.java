package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import org.junit.Assert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDPartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;

import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.util.MapperUtil.isAnyNotNull;


public class CCDPartyStatementAssert extends AbstractAssert<CCDPartyStatementAssert, CCDPartyStatement> {

    public CCDPartyStatementAssert(CCDPartyStatement actual) {
        super(actual, CCDPartyStatementAssert.class);
    }

    public CCDPartyStatementAssert isEqualTo(PartyStatement partyStatement) {
        isNotNull();

        if (null != actual.getMadeBy() && !Objects.equals(actual.getMadeBy().name(), partyStatement.getMadeBy().name())) {
            failWithMessage("Expected Party Statement.made by to be <%s> but was <%s>",
                partyStatement.getMadeBy(), actual.getMadeBy().name());
        }


        if (null != actual.getType() && !Objects.equals(actual.getType().name(), partyStatement.getType().name())) {
            failWithMessage("Expected Party Statement.type to be <%s> but was <%s>",
                partyStatement.getType().name(), actual.getType().name());
        }


        if (isAnyNotNull(actual.getOfferCompletionDate(), actual.getOfferContent(), actual.getPaymentIntention())) {
            partyStatement.getOffer().ifPresent(offer -> {
                if (!Objects.equals(offer.getCompletionDate(), actual.getOfferCompletionDate())) {
                    failWithMessage("Expected Offer completion date to be <%t> but was <%t>",
                        offer.getCompletionDate(), actual.getOfferCompletionDate());
                }

                if (!Objects.equals(offer.getContent(), actual.getOfferContent())) {
                    failWithMessage("Expected Offer content to be <%s> but was <%s>",
                        offer.getContent(), actual.getOfferContent());
                }

            });
        }

        Optional.ofNullable(actual.getPaymentIntention()).ifPresent(pymtIntention -> {
            Assertions.assertThat(partyStatement.getOffer().get().getPaymentIntention().get()).isEqualTo(actual.getPaymentIntention());
        });

        return this;
    }
}
