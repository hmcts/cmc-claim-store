package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.time.LocalDate;
import java.util.Optional;

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
        Settlement settlement = prepareSettlement(claim, responseAcceptation);
        this.offersService.signSettlementAgreement(claim.getExternalId(), settlement, authorisation);
    }

    private Settlement prepareSettlement(Claim claim, ResponseAcceptation responseAcceptation) {
        Settlement settlement = new Settlement();
        settlement.makeOffer(null, null); // TODO:
        settlement.accept(MadeBy.CLAIMANT);

        return settlement;
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
