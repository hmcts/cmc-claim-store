package uk.gov.hmcts.cmc.claimstore.model.sampledata.offers;

import uk.gov.hmcts.cmc.claimstore.models.offers.Offer;

import java.time.LocalDate;

public class SampleOffer {

    private String content = "I will fix the leaking roof";
    private LocalDate completionDate = LocalDate.now().plusDays(14);

    public static Offer validDefaults() {
        return builder().build();
    }

    public static SampleOffer builder() {
        return new SampleOffer();
    }

    public Offer build() {
        return new Offer(content, completionDate);
    }

    public SampleOffer withContent(String content) {
        this.content = content;
        return this;
    }

    public SampleOffer withCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
        return this;
    }

}
