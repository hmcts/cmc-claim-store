package uk.gov.hmcts.cmc.ccd.mapper.offers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDOffer;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;

@Component
public class OfferMapper implements Mapper<CCDOffer, Offer> {
    @Override
    public CCDOffer to(Offer offer) {
        CCDOffer.CCDOfferBuilder builder = CCDOffer.builder();
        builder.content(offer.getContent());
        builder.completionDate(offer.getCompletionDate());
        return builder.build();
    }

    @Override
    public Offer from(CCDOffer ccdOffer) {
        return new Offer(ccdOffer.getContent(), ccdOffer.getCompletionDate());
    }
}
