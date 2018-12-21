package uk.gov.hmcts.cmc.ccd.mapper.claimantresponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDClaimantResponse;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseAcceptation;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.ccd.mapper.PaymentIntentionMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Component
public class ClaimantResponseMapper {

    private final PaymentIntentionMapper paymentIntentionMapper;

    @Autowired
    public ClaimantResponseMapper(PaymentIntentionMapper paymentIntentionMapper) {
        this.paymentIntentionMapper = paymentIntentionMapper;
    }

    public CCDClaimantResponse to(ClaimantResponse claimantResponse, LocalDateTime submittedOn) {

        if (ClaimantResponseType.ACCEPTATION == claimantResponse.getType()) {
            ResponseAcceptation responseAcceptation = (ResponseAcceptation) claimantResponse;
            CCDResponseAcceptation.CCDResponseAcceptationBuilder builder = CCDResponseAcceptation.builder();
            responseAcceptation.getAmountPaid().ifPresent(builder::amountPaid);
            responseAcceptation.getFormaliseOption().ifPresent(builder::formaliseOption);

            responseAcceptation.getClaimantPaymentIntention().ifPresent(
                paymentIntention -> builder.claimantPaymentIntention(paymentIntentionMapper.to(paymentIntention))
            );

            builder.submittedOn(submittedOn);
            return builder.build();
        } else if (ClaimantResponseType.REJECTION == claimantResponse.getType()) {
            ResponseRejection responseRejection = (ResponseRejection) claimantResponse;
            CCDResponseRejection.CCDResponseRejectionBuilder builder = CCDResponseRejection.builder();
            responseRejection.getFreeMediation().ifPresent(builder::freeMediationOption);
            responseRejection.getAmountPaid().ifPresent(builder::amountPaid);
            responseRejection.getReason().ifPresent(builder::reason);
            builder.submittedOn(submittedOn);
            return builder.build();
        }
        throw new MappingException("unsupported claimant response type " + claimantResponse.getType());
    }

    public void from(CCDClaimantResponse ccdClaimantResponse, Claim.ClaimBuilder claimBuilder) {
        requireNonNull(ccdClaimantResponse, "ccdClaimantResponse must not be null");

        if (ccdClaimantResponse.getClaimantResponseType() == ClaimantResponseType.ACCEPTATION) {
            CCDResponseAcceptation ccdResponseAcceptation = (CCDResponseAcceptation) ccdClaimantResponse;
            ResponseAcceptation.ResponseAcceptationBuilder acceptationBuilder = ResponseAcceptation.builder();

            ofNullable(ccdResponseAcceptation.getClaimantPaymentIntention()).ifPresent(paymentIntention ->
                acceptationBuilder.claimantPaymentIntention(paymentIntentionMapper.from(paymentIntention))
            );

            ofNullable(ccdResponseAcceptation.getAmountPaid()).ifPresent(acceptationBuilder::amountPaid);
            ofNullable(ccdResponseAcceptation.getFormaliseOption()).ifPresent(acceptationBuilder::formaliseOption);

            claimBuilder
                .claimantResponse(acceptationBuilder.build())
                .claimantRespondedAt(ccdClaimantResponse.getSubmittedOn());
            return;

        } else if (ccdClaimantResponse.getClaimantResponseType() == ClaimantResponseType.REJECTION) {
            CCDResponseRejection ccdResponseRejection = (CCDResponseRejection) ccdClaimantResponse;
            ResponseRejection.ResponseRejectionBuilder rejectionBuilder = ResponseRejection.builder();

            ofNullable(ccdResponseRejection.getAmountPaid()).ifPresent(rejectionBuilder::amountPaid);
            ofNullable(ccdResponseRejection.getReason()).ifPresent(rejectionBuilder::reason);
            ofNullable(ccdResponseRejection.getFreeMediationOption()).ifPresent(rejectionBuilder::freeMediation);

            claimBuilder
                .claimantResponse(rejectionBuilder.build())
                .claimantRespondedAt(ccdClaimantResponse.getSubmittedOn());

            return;
        }
        throw new MappingException("Invalid claimant response type " + ccdClaimantResponse.getClaimantResponseType());
    }
}
