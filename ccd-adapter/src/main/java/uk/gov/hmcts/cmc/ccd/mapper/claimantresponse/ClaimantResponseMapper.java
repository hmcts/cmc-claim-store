package uk.gov.hmcts.cmc.ccd.mapper.claimantresponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDClaimantResponse;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDFormaliseOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseAcceptation;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.ccd.mapper.PaymentIntentionMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
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
        requireNonNull(claim, "claim must not be null");
        final ClaimantResponse claimantResponse = claim.getClaimantResponse().orElse(null);
        if (null == claimantResponse) {
            return null;
        }
        switch (claimantResponse.getType()) {
            case ACCEPTATION:
                return toAcceptation(claim, (ResponseAcceptation) claimantResponse);
            case REJECTION:
                return toRejection(claim, (ResponseRejection) claimantResponse);
            default:
                throw new MappingException("unsupported claimant response type " + claimantResponse.getType());
        }
    }

    private CCDClaimantResponse toRejection(Claim claim, ResponseRejection responseRejection) {
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

    private CCDClaimantResponse toAcceptation(Claim claim, ResponseAcceptation responseAcceptation) {
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
    }

    public void from(CCDClaimantResponse ccdClaimantResponse, Claim.ClaimBuilder claimBuilder) {
        if (null == ccdClaimantResponse) {
            return;
        }
        switch (ccdClaimantResponse.getClaimantResponseType()) {
            case ACCEPTATION:
                fromAcceptation(ccdClaimantResponse, claimBuilder);
                break;
            case REJECTION:
                fromRejection(ccdClaimantResponse, claimBuilder);
                break;
            default:
                throw new MappingException("Invalid claimant response type "
                    + ccdClaimantResponse.getClaimantResponseType());
        }
    }

    private void fromRejection(CCDClaimantResponse ccdClaimantResponse, Claim.ClaimBuilder claimBuilder) {
        CCDResponseRejection ccdResponseRejection = (CCDResponseRejection) ccdClaimantResponse;
        ResponseRejection.ResponseRejectionBuilder builder = ResponseRejection.builder()
            .amountPaid(ccdResponseRejection.getAmountPaid())
            .reason(ccdResponseRejection.getReason());
        if (ccdResponseRejection.getFreeMediationOption() != null) {
            builder.freeMediation(YesNoOption.valueOf(ccdResponseRejection.getFreeMediationOption().name()));
        }
        claimBuilder.claimantResponse(builder.build())
            .claimantRespondedAt(ccdClaimantResponse.getSubmittedOn());
    }

    private void fromAcceptation(CCDClaimantResponse ccdClaimantResponse, Claim.ClaimBuilder claimBuilder) {
        CCDResponseAcceptation ccdResponseAcceptation = (CCDResponseAcceptation) ccdClaimantResponse;
        ResponseAcceptation.ResponseAcceptationBuilder responseAcceptationBuilder = ResponseAcceptation.builder();
        responseAcceptationBuilder.amountPaid(ccdResponseAcceptation.getAmountPaid())
            .claimantPaymentIntention(paymentIntentionMapper.from(ccdResponseAcceptation
                .getClaimantPaymentIntention()));
        if (ccdResponseAcceptation.getFormaliseOption() != null) {
            responseAcceptationBuilder.formaliseOption(FormaliseOption.valueOf(ccdResponseAcceptation
                .getFormaliseOption().name()));
        }
        claimBuilder.claimantResponse(responseAcceptationBuilder.build())
            .claimantRespondedAt(ccdClaimantResponse.getSubmittedOn());
    }
}
