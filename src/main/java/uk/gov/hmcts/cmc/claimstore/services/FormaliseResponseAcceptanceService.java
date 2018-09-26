package uk.gov.hmcts.cmc.claimstore.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.time.LocalDate;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption.REFER_TO_JUDGE;

@Service
public class FormaliseResponseAcceptanceService {
    private static final Logger logger = LoggerFactory.getLogger(FormaliseResponseAcceptanceService.class);

    private final CountyCourtJudgmentService countyCourtJudgmentService;
    private final OffersService offersService;

    @Autowired
    public FormaliseResponseAcceptanceService(
        CountyCourtJudgmentService countyCourtJudgmentService,
        OffersService offersService
    ) {
        this.countyCourtJudgmentService = countyCourtJudgmentService;
        this.offersService = offersService;
    }

    public void formalise(Claim claim, ClaimantResponse claimantResponse, String authorisation) {
        if (!(claimantResponse instanceof ResponseAcceptation)) {
            return;
        }
        ResponseAcceptation responseAcceptation = (ResponseAcceptation) claimantResponse;
        FormaliseOption formaliseOption = responseAcceptation.getFormaliseOption();
        if (formaliseOption == null) {
            throw new IllegalArgumentException("formaliseOption must not be null");
        }
        switch (formaliseOption) {
            case CCJ:
                formaliseCCJ(claim, responseAcceptation, authorisation);
                break;
            case SETTLEMENT:
                formaliseSettlement(claim, responseAcceptation, authorisation);
                break;
            case REFER_TO_JUDGE:
                logger.debug("No need to formalise for " + REFER_TO_JUDGE);
                break;
            default:
                throw new IllegalStateException("Invalid formaliseOption");
        }
    }

    private void formaliseSettlement(Claim claim, ResponseAcceptation responseAcceptation, String authorisation) {
        Settlement settlement = new Settlement();
        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);
        PaymentIntention paymentIntention = acceptedPaymentIntention(responseAcceptation, response);
        DecisionType decisionType = responseAcceptation.getDecisionType();
        switch (decisionType) {
            case DEFENDANT:
                settlement.makeOffer(prepareOffer(response, paymentIntention), MadeBy.DEFENDANT);
                break;
            case CLAIMANT:
                settlement.makeOffer(prepareOffer(response, paymentIntention), MadeBy.CLAIMANT);
                break;
            case COURT:
                settlement.makeOffer(prepareOffer(response, paymentIntention), MadeBy.COURT);
                break;
            default:
                throw new IllegalStateException("Invalid determination decision type " + decisionType);

        }
        settlement.acceptCourtDetermination(MadeBy.CLAIMANT);
        this.offersService.signSettlementAgreement(claim.getExternalId(), settlement, authorisation);
    }

    private Offer prepareOffer(Response response, PaymentIntention paymentIntention) {
        Offer.OfferBuilder builder = Offer.builder();
        builder.paymentIntention(paymentIntention);

        switch (paymentIntention.getPaymentOption()) {
            case BY_SPECIFIED_DATE:
                LocalDate completionDate = paymentIntention.getPaymentDate().orElseThrow(IllegalStateException::new);
                builder.completionDate(completionDate);
                String contentBySetDate = prepareOfferContentsBySetDate(response, builder, completionDate);
                builder.content(contentBySetDate);
                break;
            case INSTALMENTS:
                RepaymentPlan repaymentPlan = paymentIntention.getRepaymentPlan().orElseThrow(IllegalAccessError::new);
                builder.completionDate(repaymentPlan.getCompletionDate());
                builder.content(String.format(
                    "%s will pay instalments of %s %s. The first instalment will be paid by %s.",
                    response.getDefendant().getName(),
                    formatMoney(repaymentPlan.getInstalmentAmount()),
                    repaymentPlan.getPaymentSchedule().getDescription(),
                    formatDate(repaymentPlan.getFirstPaymentDate())

                ));
                break;
            default:
                throw new IllegalStateException("Invalid payment option " + paymentIntention.getPaymentOption());
        }
        return builder.build();
    }

    private String prepareOfferContentsBySetDate(
        Response response,
        Offer.OfferBuilder builder,
        LocalDate completionDate
    ) {
        builder.completionDate(completionDate);
        String amount;
        switch (response.getResponseType()) {
            case PART_ADMISSION:
                PartAdmissionResponse partAdmissionResponse = (PartAdmissionResponse) response;
                amount = formatMoney(partAdmissionResponse.getAmount());
                break;
            case FULL_ADMISSION:
                amount = "the full amount";
                break;
            default:
                throw new IllegalStateException("Invalid response type " + response.getResponseType());
        }
        return String.format("%s will pay %s, no later than %s",
            response.getDefendant().getName(), amount, formatDate(completionDate)
        );
    }

    private void formaliseCCJ(Claim claim, ResponseAcceptation responseAcceptation, String authorisation) {
        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);
        PaymentIntention acceptedPaymentIntention = acceptedPaymentIntention(responseAcceptation, response);

        CountyCourtJudgment countyCourtJudgment = CountyCourtJudgment.builder()
            .defendantDateOfBirth(defendantDateOfBirth(response.getDefendant()))
            .paymentOption(acceptedPaymentIntention.getPaymentOption())
            .paidAmount(responseAcceptation.getAmountPaid().orElse(null))
            .repaymentPlan(acceptedPaymentIntention.getRepaymentPlan().orElse(null))
            .payBySetDate(acceptedPaymentIntention.getPaymentDate().orElse(null))
            .build();

        this.countyCourtJudgmentService.save(
            claim.getSubmitterId(),
            countyCourtJudgment,
            claim.getExternalId(),
            authorisation,
            true);
    }

    private LocalDate defendantDateOfBirth(Party party) {
        if (party instanceof Individual) {
            return ((Individual) party).getDateOfBirth();
        }
        return null;
    }

    private PaymentIntention acceptedPaymentIntention(ResponseAcceptation responseAcceptation, Response response) {
        DecisionType decisionType = responseAcceptation.getDecisionType();
        if (decisionType == null) {
            throw new IllegalArgumentException("formaliseOption must not be null");
        }
        switch (decisionType) {
            case DEFENDANT:
                return getDefendantPaymentIntention(response);
            case CLAIMANT:
                return responseAcceptation.getClaimantPaymentIntention().orElseThrow(IllegalStateException::new);
            case COURT:
                return responseAcceptation
                    .getCourtDetermination().orElseThrow(IllegalStateException::new)
                    .getCourtDecision();
            default:
                throw new IllegalStateException("Invalid determination decision type " + decisionType);
        }
    }

    private PaymentIntention getDefendantPaymentIntention(Response response) {
        switch (response.getResponseType()) {
            case PART_ADMISSION:
                return ((PartAdmissionResponse) response).getPaymentIntention().orElseThrow(IllegalStateException::new);
            case FULL_ADMISSION:
                return ((FullAdmissionResponse) response).getPaymentIntention();
            default:
                throw new IllegalStateException("Invalid response type " + response.getResponseType());
        }
    }
}
