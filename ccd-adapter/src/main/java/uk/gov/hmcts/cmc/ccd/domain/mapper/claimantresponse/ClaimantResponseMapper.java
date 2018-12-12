package uk.gov.hmcts.cmc.ccd.domain.mapper.claimantresponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDClaimantResponse;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDClaimantResponseType;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDFormaliseOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseAcceptation;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.domain.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.domain.mapper.PaymentIntentionMapper;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;

@Component
public class ClaimantResponseMapper implements Mapper<CCDClaimantResponse, ClaimantResponse> {

    private final PaymentIntentionMapper paymentIntentionMapper;
    private final CourtDeterminationMapper courtDeterminationMapper;

    @Autowired
    public ClaimantResponseMapper(PaymentIntentionMapper paymentIntentionMapper,
                                  CourtDeterminationMapper courtDeterminationMapper) {

        this.paymentIntentionMapper = paymentIntentionMapper;
        this.courtDeterminationMapper = courtDeterminationMapper;
    }

    @Override
    public CCDClaimantResponse to(ClaimantResponse claimantResponse) {
        if (claimantResponse == null) {
            return null;
        }
        if (ClaimantResponseType.ACCEPTATION == claimantResponse.getType()) {
            ResponseAcceptation responseAcceptation = (ResponseAcceptation) claimantResponse;
            CCDResponseAcceptation.CCDResponseAcceptationBuilder builder = CCDResponseAcceptation.builder();
            responseAcceptation.getAmountPaid().ifPresent(builder::amountPaid);
            responseAcceptation.getFormaliseOption()
                .map(FormaliseOption::name)
                .map(CCDFormaliseOption::valueOf)
                .ifPresent(builder::formaliseOption);
            responseAcceptation.getClaimantPaymentIntention().ifPresent(
                paymentIntention -> builder.claimantPaymentIntention(paymentIntentionMapper.to(paymentIntention))
            );
            responseAcceptation.getCourtDetermination().ifPresent(
                courtDetermination -> builder.courtDetermination(courtDeterminationMapper.to(courtDetermination))
            );
            return builder.build();
        } else if (ClaimantResponseType.REJECTION == claimantResponse.getType()) {
            ResponseRejection responseRejection = (ResponseRejection) claimantResponse;
            Boolean mediation = responseRejection.getFreeMediation().orElse(CCDYesNoOption.NO.toBoolean());
            CCDResponseRejection.CCDResponseRejectionBuilder rejection = CCDResponseRejection.builder()
                .freeMediationOption(CCDYesNoOption.valueOf(mediation));
            responseRejection.getAmountPaid().ifPresent(rejection::amountPaid);
            responseRejection.getReason().ifPresent(rejection::reason);
            return rejection.build();
        }
        throw new MappingException("unsupported claimant response type " + claimantResponse.getType());
    }

    @Override
    public ClaimantResponse from(CCDClaimantResponse ccdClaimantResponse) {
        if (null == ccdClaimantResponse) {
            return null;
        }
        if (ccdClaimantResponse.getClaimantResponseType() == CCDClaimantResponseType.ACCEPTATION) {
            CCDResponseAcceptation ccdResponseAcceptation = (CCDResponseAcceptation) ccdClaimantResponse;
            ResponseAcceptation.builder()
                .amountPaid(ccdResponseAcceptation.getAmountPaid())
                .formaliseOption(FormaliseOption.valueOf(ccdResponseAcceptation.getFormaliseOption().name()))
                .claimantPaymentIntention(paymentIntentionMapper.from(ccdResponseAcceptation
                    .getClaimantPaymentIntention()))
                .courtDetermination(courtDeterminationMapper.from(ccdResponseAcceptation.getCourtDetermination()))
                .build();
        } else if (ccdClaimantResponse.getClaimantResponseType() == CCDClaimantResponseType.REJECTION) {
            CCDResponseRejection ccdResponseRejection = (CCDResponseRejection) ccdClaimantResponse;
            ResponseRejection.ResponseRejectionBuilder builder = ResponseRejection.builder()
                .amountPaid(ccdResponseRejection.getAmountPaid())
                .reason(ccdResponseRejection.getReason());
            if (ccdResponseRejection.getFreeMediationOption() != null) {
                builder.freeMediation(ccdResponseRejection.getFreeMediationOption().toBoolean());
            }
            return builder.build();
        }
        throw new MappingException("Invalid claimant response type " + ccdClaimantResponse.getClaimantResponseType());
    }
}
