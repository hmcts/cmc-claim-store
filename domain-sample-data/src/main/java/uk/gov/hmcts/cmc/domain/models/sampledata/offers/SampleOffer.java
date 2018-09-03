package uk.gov.hmcts.cmc.domain.models.sampledata.offers;

import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.time.LocalDate;

public class SampleOffer {

    private String content = "I will fix the leaking roof";
    private LocalDate completionDate = LocalDate.now().plusDays(14);
    private PaymentIntention paymentIntention;

    public static Offer validDefaults() {
        return builder().build();
    }

    public static SampleOffer builder() {
        return new SampleOffer();
    }

    public Offer build() {
        return new Offer(content, completionDate, paymentIntention);
    }

    public SampleOffer withContent(String content) {
        this.content = content;
        return this;
    }

    public SampleOffer withCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
        return this;
    }

    public SampleOffer withPaymentIntention(PaymentIntention paymentIntention) {
        this.paymentIntention = paymentIntention;
        return this;
    }

}
