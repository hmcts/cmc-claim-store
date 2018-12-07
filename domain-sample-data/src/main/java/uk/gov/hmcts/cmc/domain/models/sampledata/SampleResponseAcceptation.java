package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation.ResponseAcceptationBuilder;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SampleCourtDetermination;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;

import java.math.BigDecimal;

public class SampleResponseAcceptation {

    private SampleResponseAcceptation(){}

    public static ResponseAcceptationBuilder builder() {
        return ResponseAcceptation.builder();
    }

    public static ResponseAcceptation partAdmitPayImmediately() {
        return builder().formaliseOption(FormaliseOption.SETTLEMENT)
            .amountPaid(new BigDecimal(100))
            .build();
    }

    public static ResponseAcceptation partAdmitPayBySetDate() {
        return builder().formaliseOption(FormaliseOption.SETTLEMENT)
            .amountPaid(new BigDecimal(100))
            .courtDetermination(SampleCourtDetermination.instalments())
            .claimantPaymentIntention(SamplePaymentIntention.bySetDate())
            .build();
    }

    public static ResponseAcceptation partAdmitPayByInstalments() {
        return builder().formaliseOption(FormaliseOption.SETTLEMENT)
            .amountPaid(new BigDecimal(100))
            .courtDetermination(SampleCourtDetermination.instalments())
            .claimantPaymentIntention(SamplePaymentIntention.instalments())
            .build();
    }

}
