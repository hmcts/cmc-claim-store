package uk.gov.hmcts.cmc.domain.models.sampledata.offers;

import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.Offer.OfferBuilder;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;

import java.time.LocalDate;

public class SampleOffer {

    private SampleOffer() {
        super();
    }

    public static OfferBuilder builder() {
        return Offer.builder()
            .content("I will fix the leaking roof")
            .completionDate(LocalDate.now().plusDays(14));
    }

    public static OfferBuilder builderWithPaymentIntention() {
        return builder()
            .paymentIntention(SamplePaymentIntention.instalments());
    }
}
