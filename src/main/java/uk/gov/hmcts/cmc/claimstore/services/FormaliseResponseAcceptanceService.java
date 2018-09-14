package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
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
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
import static uk.gov.hmcts.cmc.domain.models.offers.MadeBy.CLAIMANT;
import static uk.gov.hmcts.cmc.domain.models.offers.MadeBy.COURT;
import static uk.gov.hmcts.cmc.domain.models.offers.MadeBy.DEFENDANT;

@Service
public class FormaliseResponseAcceptanceService {

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

    public void formalise(Claim claim, ResponseAcceptation responseAcceptation, String authorisation) {
        switch (responseAcceptation.getFormaliseOption()) {
            case CCJ:
                formaliseCCJ(claim, responseAcceptation, authorisation);
                break;
            case SETTLEMENT:
                formaliseSettlement(claim, responseAcceptation, authorisation);
                break;
            default:
                throw new IllegalStateException("Can't formalise for " + responseAcceptation.getFormaliseOption());
        }
    }

    private void formaliseSettlement(Claim claim, ResponseAcceptation responseAcceptation, String authorisation) {
        Settlement settlement = new Settlement();
        Optional<CourtDetermination> courtDetermination = responseAcceptation.getCourtDetermination();
        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);
        PaymentIntention paymentIntention;
        if (courtDetermination.isPresent()) {
            paymentIntention = courtDetermination.get().getCourtCalculatedPaymentIntention();
            settlement.makeOffer(prepareOffer(response, paymentIntention), COURT);
        } else if (responseAcceptation.getClaimantPaymentIntention().isPresent()) {
            paymentIntention = responseAcceptation.getClaimantPaymentIntention().get();
            settlement.makeOffer(prepareOffer(response, paymentIntention), CLAIMANT);
        } else {
            paymentIntention = getDefendantPaymentIntention(claim.getResponse().orElseThrow(IllegalAccessError::new));
            settlement.makeOffer(prepareOffer(response, paymentIntention), DEFENDANT);
        }
        settlement.accept(MadeBy.CLAIMANT);
        this.offersService.signSettlementAgreement(claim.getExternalId(), settlement, authorisation);
    }

    private Offer prepareOffer(Response response, PaymentIntention paymentIntention) {
        Offer.OfferBuilder builder = Offer.builder();
        builder.paymentIntention(paymentIntention);

        switch (paymentIntention.getPaymentOption()) {
            case BY_SPECIFIED_DATE:
                LocalDate completionDate = paymentIntention.getPaymentDate().orElseThrow(IllegalStateException::new);
                builder.completionDate(completionDate);
                String amount;
                if (response instanceof PartAdmissionResponse) {
                    PartAdmissionResponse partAdmissionResponse = (PartAdmissionResponse) response;
                    amount = formatMoney(partAdmissionResponse.getAmount());
                } else {
                    amount = "the full amount";
                }
                builder.content(
                    String.format("%s will pay %s, no later than %s",
                        response.getDefendant().getName(), amount, formatDate(completionDate)
                    )
                );
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

    private void formaliseCCJ(Claim claim, ResponseAcceptation responseAcceptation, String authorisation) {
        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);
        PaymentIntention acceptedPaymentIntention = acceptedPaymentIntention(responseAcceptation, response);

        CountyCourtJudgment countyCourtJudgment = CountyCourtJudgment.builder()
            .defendantDateOfBirth(defendantDateOfBirth(response.getDefendant()))
            .paymentOption(acceptedPaymentIntention.getPaymentOption())
            .paidAmount(responseAcceptation.getAmountPaid())
            .repaymentPlan(acceptedPaymentIntention.getRepaymentPlan().orElse(null))
            .payBySetDate(acceptedPaymentIntention.getPaymentDate().orElse(null))
            .build();

        boolean issued = true;
        this.countyCourtJudgmentService.save(
            claim.getSubmitterId(),
            countyCourtJudgment,
            claim.getExternalId(),
            authorisation,
            issued);
    }

    private LocalDate defendantDateOfBirth(Party party) {
        if (party instanceof Individual) {
            return ((Individual) party).getDateOfBirth();
        }
        return null;
    }

    private PaymentIntention acceptedPaymentIntention(ResponseAcceptation responseAcceptation, Response response) {
        Optional<CourtDetermination> courtDetermination = responseAcceptation.getCourtDetermination();
        if (courtDetermination.isPresent()) {
            return courtDetermination.get().getCourtCalculatedPaymentIntention();
        } else if (responseAcceptation.getClaimantPaymentIntention().isPresent()) {
            return responseAcceptation.getClaimantPaymentIntention().get();
        } else {
            return getDefendantPaymentIntention(response);
        }
    }

    private PaymentIntention getDefendantPaymentIntention(Response response) {
        if (response instanceof FullAdmissionResponse) {
            return ((FullAdmissionResponse) response).getPaymentIntention();
        } else if (response instanceof PartAdmissionResponse) {
            return ((PartAdmissionResponse) response).getPaymentIntention().orElse(null);
        } else {
            return null;
        }
    }
}
