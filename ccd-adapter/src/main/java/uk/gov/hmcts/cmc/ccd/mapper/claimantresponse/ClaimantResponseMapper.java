package uk.gov.hmcts.cmc.ccd.mapper.claimantresponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDClaimantResponse;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDFormaliseOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseAcceptation;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.ccd.mapper.DirectionsQuestionnaireMapper;
import uk.gov.hmcts.cmc.ccd.mapper.MoneyMapper;
import uk.gov.hmcts.cmc.ccd.mapper.PaymentIntentionMapper;
import uk.gov.hmcts.cmc.ccd.mapper.TelephoneMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Component
public class ClaimantResponseMapper {

    private final PaymentIntentionMapper paymentIntentionMapper;
    private final CourtDeterminationMapper courtDeterminationMapper;
    private final TelephoneMapper telephoneMapper;
    private final MoneyMapper moneyMapper;
    private final DirectionsQuestionnaireMapper directionsQuestionnaireMapper;

    @Autowired
    public ClaimantResponseMapper(
        PaymentIntentionMapper paymentIntentionMapper,
        CourtDeterminationMapper courtDeterminationMapper,
        TelephoneMapper telephoneMapper,
        MoneyMapper moneyMapper,
        DirectionsQuestionnaireMapper directionsQuestionnaireMapper
    ) {
        this.paymentIntentionMapper = paymentIntentionMapper;
        this.courtDeterminationMapper = courtDeterminationMapper;
        this.telephoneMapper = telephoneMapper;
        this.moneyMapper = moneyMapper;
        this.directionsQuestionnaireMapper = directionsQuestionnaireMapper;
    }

    public CCDClaimantResponse to(Claim claim) {
        requireNonNull(claim, "claim must not be null");

        Optional<ClaimantResponse> claimantResponseOptional = claim.getClaimantResponse();
        if (claimantResponseOptional.isPresent()) {
            final ClaimantResponse claimantResponse = claimantResponseOptional.get();
            switch (claimantResponse.getType()) {
                case ACCEPTATION:
                    return toAcceptation(claim, (ResponseAcceptation) claimantResponse);
                case REJECTION:
                    return toRejection(claim, (ResponseRejection) claimantResponse);
                default:
                    throw new MappingException("unsupported claimant response type " + claimantResponse.getType());
            }
        } else {
            return null;
        }
    }

    private CCDClaimantResponse toRejection(Claim claim, ResponseRejection responseRejection) {
        CCDResponseRejection.CCDResponseRejectionBuilder rejection = CCDResponseRejection.builder();
        responseRejection.getFreeMediation()
            .map(YesNoOption::name)
            .map(CCDYesNoOption::valueOf)
            .ifPresent(rejection::freeMediationOption);
        responseRejection.getMediationContactPerson().ifPresent(rejection::mediationContactPerson);
        responseRejection.getMediationPhoneNumber()
            .ifPresent(phoneNo -> rejection.mediationPhoneNumber(telephoneMapper.to(phoneNo)));
        responseRejection.getNoMediationReason().ifPresent(rejection::noMediationReason);

        responseRejection.getAmountPaid().map(moneyMapper::to).ifPresent(rejection::amountPaid);

        responseRejection.getReason().ifPresent(rejection::reason);
        responseRejection.getPaymentReceived()
            .map(YesNoOption::name)
            .map(CCDYesNoOption::valueOf)
            .ifPresent(rejection::paymentReceived);
        responseRejection.getSettleForAmount()
            .map(YesNoOption::name)
            .map(CCDYesNoOption::valueOf)
            .ifPresent(rejection::settleForAmount);
        responseRejection.getDirectionsQuestionnaire()
            .map(directionsQuestionnaireMapper::to)
            .ifPresent(rejection::directionsQuestionnaire);
        claim.getClaimantRespondedAt().ifPresent(rejection::submittedOn);
        return rejection.build();
    }

    private CCDClaimantResponse toAcceptation(Claim claim, ResponseAcceptation responseAcceptation) {
        CCDResponseAcceptation.CCDResponseAcceptationBuilder builder = CCDResponseAcceptation.builder();
        responseAcceptation.getAmountPaid().map(moneyMapper::to).ifPresent(builder::amountPaid);

        responseAcceptation.getFormaliseOption()
            .map(FormaliseOption::name)
            .map(CCDFormaliseOption::valueOf)
            .ifPresent(builder::formaliseOption);
        responseAcceptation.getClaimantPaymentIntention().ifPresent(
            paymentIntention -> builder.claimantPaymentIntention(paymentIntentionMapper.to(paymentIntention))
        );
        responseAcceptation.getCourtDetermination().ifPresent(courtDetermination ->
            builder.courtDetermination(courtDeterminationMapper.to(courtDetermination)));

        responseAcceptation.getPaymentReceived()
            .map(YesNoOption::name)
            .map(CCDYesNoOption::valueOf)
            .ifPresent(builder::paymentReceived);
        responseAcceptation.getSettleForAmount()
            .map(YesNoOption::name)
            .map(CCDYesNoOption::valueOf)
            .ifPresent(builder::settleForAmount);

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
            .amountPaid(moneyMapper.from(ccdResponseRejection.getAmountPaid()))
            .reason(ccdResponseRejection.getReason());
        if (ccdResponseRejection.getFreeMediationOption() != null) {
            builder.freeMediation(YesNoOption.valueOf(ccdResponseRejection.getFreeMediationOption().name()));
            builder.mediationPhoneNumber(telephoneMapper.from(ccdResponseRejection.getMediationPhoneNumber()));
            builder.mediationContactPerson(ccdResponseRejection.getMediationContactPerson());
            builder.noMediationReason(ccdResponseRejection.getNoMediationReason());
        }

        if (ccdResponseRejection.getPaymentReceived() != null) {
            builder.paymentReceived(YesNoOption.valueOf(ccdResponseRejection.getPaymentReceived().name()));
        }

        if (ccdResponseRejection.getSettleForAmount() != null) {
            builder.settleForAmount(YesNoOption.valueOf(ccdResponseRejection.getSettleForAmount().name()));
        }

        builder.directionsQuestionnaire(
            directionsQuestionnaireMapper.from(ccdResponseRejection.getDirectionsQuestionnaire()));

        claimBuilder.claimantResponse(builder.build())
            .claimantRespondedAt(ccdClaimantResponse.getSubmittedOn());
    }

    private void fromAcceptation(CCDClaimantResponse ccdClaimantResponse, Claim.ClaimBuilder claimBuilder) {
        CCDResponseAcceptation ccdResponseAcceptation = (CCDResponseAcceptation) ccdClaimantResponse;
        ResponseAcceptation.ResponseAcceptationBuilder responseAcceptationBuilder = ResponseAcceptation.builder();

        responseAcceptationBuilder
            .amountPaid(moneyMapper.from(ccdResponseAcceptation.getAmountPaid()))
            .claimantPaymentIntention(paymentIntentionMapper.from(ccdResponseAcceptation.getClaimantPaymentIntention()))
            .courtDetermination(courtDeterminationMapper.from(ccdResponseAcceptation.getCourtDetermination()));

        if (ccdResponseAcceptation.getFormaliseOption() != null) {
            responseAcceptationBuilder.formaliseOption(FormaliseOption.valueOf(ccdResponseAcceptation
                .getFormaliseOption().name()));
        }

        if (ccdResponseAcceptation.getPaymentReceived() != null) {
            responseAcceptationBuilder.paymentReceived(
                YesNoOption.valueOf(ccdResponseAcceptation.getPaymentReceived().name())
            );
        }

        if (ccdResponseAcceptation.getSettleForAmount() != null) {
            responseAcceptationBuilder.settleForAmount(
                YesNoOption.valueOf(ccdResponseAcceptation.getSettleForAmount().name())
            );
        }

        claimBuilder
            .claimantResponse(responseAcceptationBuilder.build())
            .claimantRespondedAt(ccdClaimantResponse.getSubmittedOn());
    }
}
