package uk.gov.hmcts.cmc.ccd.mapper.claimantresponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDClaimantResponse;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDClaimantResponseType;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDFormaliseOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseAcceptation;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.ccd.mapper.PaymentIntentionMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import static java.util.Objects.requireNonNull;

@Component
public class ClaimantResponseMapper {

    private final PaymentIntentionMapper paymentIntentionMapper;

    @Autowired
    public ClaimantResponseMapper(PaymentIntentionMapper paymentIntentionMapper) {
        this.paymentIntentionMapper = paymentIntentionMapper;
    }

    public CCDClaimantResponse to(Claim claim) {
        requireNonNull(claim,"claim must not be null");
        final ClaimantResponse claimantResponse = claim.getClaimantResponse().orElse(null);
        if (null == claimantResponse) {
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
            claim.getClaimantRespondedAt().ifPresent(builder::submittedOn);
            return builder.build();
        } else if (ClaimantResponseType.REJECTION == claimantResponse.getType()) {
            ResponseRejection responseRejection = (ResponseRejection) claimantResponse;
            CCDResponseRejection.CCDResponseRejectionBuilder rejection = CCDResponseRejection.builder();
            responseRejection.getFreeMediation()
                .map(YesNoOption::name)
                .map(CCDYesNoOption::valueOf)
                .ifPresent(rejection::freeMediationOption);
            responseRejection.getAmountPaid().ifPresent(rejection::amountPaid);
            responseRejection.getReason().ifPresent(rejection::reason);
            claim.getClaimantRespondedAt().ifPresent(rejection::submittedOn);
            return rejection.build();
        }
        throw new MappingException("unsupported claimant response type " + claimantResponse.getType());
    }

    public void from(CCDClaimantResponse ccdClaimantResponse,Claim.ClaimBuilder claimBuilder) {
        if (null == ccdClaimantResponse) {
            return;
        }
        if (ccdClaimantResponse.getClaimantResponseType() == CCDClaimantResponseType.ACCEPTATION) {
            CCDResponseAcceptation ccdResponseAcceptation = (CCDResponseAcceptation) ccdClaimantResponse;
            claimBuilder.claimantResponse(ResponseAcceptation.builder()
                .amountPaid(ccdResponseAcceptation.getAmountPaid())
                .formaliseOption(FormaliseOption.valueOf(ccdResponseAcceptation.getFormaliseOption().name()))
                .claimantPaymentIntention(paymentIntentionMapper.from(ccdResponseAcceptation
                    .getClaimantPaymentIntention()))
                .build())
                .claimantRespondedAt(ccdClaimantResponse.getSubmittedOn());
        } else if (ccdClaimantResponse.getClaimantResponseType() == CCDClaimantResponseType.REJECTION) {
            CCDResponseRejection ccdResponseRejection = (CCDResponseRejection) ccdClaimantResponse;
            ResponseRejection.ResponseRejectionBuilder builder = ResponseRejection.builder()
                .amountPaid(ccdResponseRejection.getAmountPaid())
                .reason(ccdResponseRejection.getReason());
            if (ccdResponseRejection.getFreeMediationOption() != null) {
                builder.freeMediation(YesNoOption.valueOf(ccdResponseRejection.getFreeMediationOption().name()));
            }
            claimBuilder.claimantResponse(builder.build())
                .claimantRespondedAt(ccdClaimantResponse.getSubmittedOn());
        } else {
            throw new MappingException("Invalid claimant response type "
                            + ccdClaimantResponse.getClaimantResponseType());
        }
    }
}
