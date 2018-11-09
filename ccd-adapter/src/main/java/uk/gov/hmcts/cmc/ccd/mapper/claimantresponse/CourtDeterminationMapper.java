package uk.gov.hmcts.cmc.ccd.mapper.claimantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDCourtDetermination;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.mapper.response.PaymentIntentionMapper;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;

@Component
public class CourtDeterminationMapper implements Mapper<CCDCourtDetermination, CourtDetermination> {
    private final PaymentIntentionMapper paymentIntentionMapper;

    public CourtDeterminationMapper(PaymentIntentionMapper paymentIntentionMapper) {
        this.paymentIntentionMapper = paymentIntentionMapper;
    }

    @Override
    public CCDCourtDetermination to(CourtDetermination courtDetermination) {
        CCDCourtDetermination.CCDCourtDeterminationBuilder builder = CCDCourtDetermination.builder()
            .courtDecision(paymentIntentionMapper.to(courtDetermination.getCourtDecision()))
            .disposableIncome(courtDetermination.getDisposableIncome())
            .courtPaymentIntention(paymentIntentionMapper.to(courtDetermination.getCourtPaymentIntention()));

        courtDetermination.getRejectionReason().ifPresent(builder::rejectionReason);
        return builder.build();
    }

    @Override
    public CourtDetermination from(CCDCourtDetermination ccdCourtDetermination) {
        CourtDetermination.CourtDeterminationBuilder builder = CourtDetermination.builder()
            .courtDecision(paymentIntentionMapper.from(ccdCourtDetermination.getCourtDecision()))
            .disposableIncome(ccdCourtDetermination.getDisposableIncome())
            .courtPaymentIntention(paymentIntentionMapper.from(ccdCourtDetermination.getCourtPaymentIntention()));

        if (ccdCourtDetermination.getRejectionReason() != null) {
            builder.rejectionReason(ccdCourtDetermination.getRejectionReason());
        }
        return builder.build();
    }
}
