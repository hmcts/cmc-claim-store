package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import org.junit.Assert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDPartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.util.MapperUtil.isAllNull;
import static uk.gov.hmcts.cmc.ccd.util.MapperUtil.isAnyNotNull;


public class CCDPartyStatementAssert extends AbstractAssert<CCDPartyStatementAssert, CCDPartyStatement> {

    public CCDPartyStatementAssert(CCDPartyStatement actual) {
        super(actual, CCDPartyStatementAssert.class);
    }

    public CCDPartyStatementAssert isEqualTo(PartyStatement partyStatement) {
        isNotNull();

        if (!Objects.equals(actual.getMadeBy().name(), partyStatement.getMadeBy().name())) {
            failWithMessage("Expected Party Statement.made by to be <%s> but was <%s>",
                partyStatement.getMadeBy(), actual.getMadeBy().name());
        }


        if (!Objects.equals(actual.getType().name(), partyStatement.getType().name())) {
            failWithMessage("Expected Party Statement.type to be <%s> but was <%s>",
                partyStatement.getType().name(), actual.getType().name());
        }


        if(isAnyNotNull(actual.getOfferCompletionDate(), actual.getOfferContent(), actual.getPaymentIntention())){

        }

        actual.getOffer().ifPresent(offer -> {
                Assert.assertEquals(offer.getContent(), ccdPartyStatement.getOfferContent());
                Assert.assertEquals(offer.getCompletionDate(), ccdPartyStatement.getOfferCompletionDate());

                offer.getPaymentIntention().ifPresent(
                    paymentIntention -> assertThat(paymentIntention).isEqualTo(ccdPartyStatement.getPaymentIntention())
                );
            }
        );

        return this;
    }
}
