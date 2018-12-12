package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.offers.CCDOffer;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;

import java.util.Objects;

public class OfferAssert extends AbstractAssert<OfferAssert, Offer> {

    public OfferAssert(Offer actual) {
        super(actual, OfferAssert.class);
    }

    public OfferAssert isEqualTo(CCDOffer ccdOffer) {
        isNotNull();

        if (!Objects.equals(actual.getContent(), ccdOffer.getContent())) {
            failWithMessage("Expected Offer.content to be <%s> but was <%s>",
                ccdOffer.getContent(), actual.getContent());
        }

        if (!Objects.equals(actual.getCompletionDate(), ccdOffer.getCompletionDate())) {
            failWithMessage("Expected Offer.completionDate to be <%s> but was <%s>",
                ccdOffer.getCompletionDate(), actual.getCompletionDate());
        }

        return this;
    }
}
