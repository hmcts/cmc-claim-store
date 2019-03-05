package uk.gov.hmcts.cmc.ccd.mapper.claimantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDCourtDetermination;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDDecisionType;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.mapper.PaymentIntentionMapper;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;

@Component
public class CourtDeterminationMapper implements Mapper<CCDCourtDetermination, CourtDetermination> {
    private final PaymentIntentionMapper paymentIntentionMapper;

    public CourtDeterminationMapper(PaymentIntentionMapper paymentIntentionMapper) {
        this.paymentIntentionMapper = paymentIntentionMapper;
    }

    @Override
    public CCDCourtDetermination to(CourtDetermination courtDetermination) {
        if (null == courtDetermination) {
            return null;
        }
        CCDCourtDetermination.CCDCourtDeterminationBuilder builder = CCDCourtDetermination.builder()
            .courtDecision(paymentIntentionMapper.to(courtDetermination.getCourtDecision()))
            .disposableIncome(courtDetermination.getDisposableIncome())
            .decisionType(CCDDecisionType.valueOf(courtDetermination.getDecisionType().name()))
            .courtIntention(paymentIntentionMapper.to(courtDetermination.getCourtPaymentIntention()));
        courtDetermination.getRejectionReason().ifPresent(builder::rejectionReason);
        return builder.build();
    }

    @Override
    public CourtDetermination from(CCDCourtDetermination ccdCourtDetermination) {
        if (null == ccdCourtDetermination) {
            return null;
        }
        CourtDetermination.CourtDeterminationBuilder builder = CourtDetermination.builder()
            .courtDecision(paymentIntentionMapper.from(ccdCourtDetermination.getCourtDecision()))
            .disposableIncome(ccdCourtDetermination.getDisposableIncome())
            .courtPaymentIntention(paymentIntentionMapper.from(ccdCourtDetermination.getCourtIntention()))
            .decisionType(DecisionType.valueOf(ccdCourtDetermination.getDecisionType().name()))
            .rejectionReason(ccdCourtDetermination.getRejectionReason());
        return builder.build();
    }
}
