package uk.gov.hmcts.cmc.ccd.deprecated.mapper.offers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.offers.CCDOffer;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;

@Component
public class OfferMapper implements Mapper<CCDOffer, Offer> {
    @Override
    public CCDOffer to(Offer offer) {
        return CCDOffer.builder()
            .content(offer.getContent())
            .completionDate(offer.getCompletionDate())
            .build();
    }

    @Override
    public Offer from(CCDOffer ccdOffer) {
        return new Offer(ccdOffer.getContent(), ccdOffer.getCompletionDate(), null);
    }
}
