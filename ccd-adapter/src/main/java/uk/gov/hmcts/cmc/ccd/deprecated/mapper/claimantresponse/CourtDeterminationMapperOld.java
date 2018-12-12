package uk.gov.hmcts.cmc.ccd.deprecated.mapper.claimantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDCourtDetermination;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.response.PaymentIntentionMapperOld;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;

@Component
public class CourtDeterminationMapperOld implements Mapper<CCDCourtDetermination, CourtDetermination> {
    private final PaymentIntentionMapperOld paymentIntentionMapperOld;

    public CourtDeterminationMapperOld(PaymentIntentionMapperOld paymentIntentionMapperOld) {
        this.paymentIntentionMapperOld = paymentIntentionMapperOld;
    }

    @Override
    public CCDCourtDetermination to(CourtDetermination courtDetermination) {
        CCDCourtDetermination.CCDCourtDeterminationBuilder builder = CCDCourtDetermination.builder()
            .courtDecision(paymentIntentionMapperOld.to(courtDetermination.getCourtDecision()))
            .disposableIncome(courtDetermination.getDisposableIncome())
            .courtPaymentIntention(paymentIntentionMapperOld.to(courtDetermination.getCourtPaymentIntention()));

        courtDetermination.getRejectionReason().ifPresent(builder::rejectionReason);
        return builder.build();
    }

    @Override
    public CourtDetermination from(CCDCourtDetermination ccdCourtDetermination) {
        CourtDetermination.CourtDeterminationBuilder builder = CourtDetermination.builder()
            .courtDecision(paymentIntentionMapperOld.from(ccdCourtDetermination.getCourtDecision()))
            .disposableIncome(ccdCourtDetermination.getDisposableIncome())
            .courtPaymentIntention(paymentIntentionMapperOld.from(ccdCourtDetermination.getCourtPaymentIntention()));

        if (ccdCourtDetermination.getRejectionReason() != null) {
            builder.rejectionReason(ccdCourtDetermination.getRejectionReason());
        }
        return builder.build();
    }
}
