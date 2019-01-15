package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.events.CCDEventProducer;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;
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
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INTERLOCATORY_JUDGEMENT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REJECT_ORGANISATION_PAYMENT_PLAN;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
import static uk.gov.hmcts.cmc.domain.utils.PartyUtils.isCompanyOrOrganisation;

@Service
public class FormaliseResponseAcceptanceService {

    private final CountyCourtJudgmentService countyCourtJudgmentService;
    private final OffersService offersService;
    private final EventProducer eventProducer;
    private final CCDEventProducer ccdEventProducer;
    private final CaseRepository caseRepository;

    @Autowired
    public FormaliseResponseAcceptanceService(
        CountyCourtJudgmentService countyCourtJudgmentService,
        OffersService offersService,
        EventProducer eventProducer,
        CCDEventProducer ccdEventProducer,
        CaseRepository caseRepository
    ) {
        this.countyCourtJudgmentService = countyCourtJudgmentService;
        this.offersService = offersService;
        this.eventProducer = eventProducer;
        this.ccdEventProducer = ccdEventProducer;
        this.caseRepository = caseRepository;
    }

    public void formalise(Claim claim, ResponseAcceptation responseAcceptation, String authorisation) {
        switch (responseAcceptation.getFormaliseOption().orElseThrow(IllegalStateException::new)) {
            case CCJ:
                formaliseCCJ(claim, responseAcceptation, authorisation);
                break;
            case SETTLEMENT:
                formaliseSettlement(claim, responseAcceptation, authorisation);
                break;
            case REFER_TO_JUDGE:
                createEventForReferToJudge(claim, authorisation);
                break;
            default:
                throw new IllegalStateException("Invalid formaliseOption");
        }
    }

    private void createEventForReferToJudge(Claim claim, String authorisation) {
        Response response = claim.getResponse().orElseThrow(IllegalArgumentException::new);
        CaseEvent caseEvent;
        if (isCompanyOrOrganisation(response.getDefendant())) {
            eventProducer.createRejectOrganisationPaymentPlanEvent(claim);
            caseEvent = INTERLOCATORY_JUDGEMENT;
            ccdEventProducer.createCCDRejectOrganisationPaymentPlanEvent(claim, authorisation);
        } else {
            eventProducer.createInterlocutoryJudgmentEvent(claim);
            caseEvent = REJECT_ORGANISATION_PAYMENT_PLAN;
            ccdEventProducer.createCCDInterlocutoryJudgmentEvent(claim, authorisation);
        }
        this.caseRepository.saveCaseEvent(authorisation, claim, caseEvent);
    }

    private void formaliseSettlement(Claim claim, ResponseAcceptation responseAcceptation, String authorisation) {
        Settlement settlement = new Settlement();
        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);
        PaymentIntention paymentIntention = acceptedPaymentIntention(responseAcceptation, response);
        BigDecimal claimAmountTillDate = claim.getTotalAmountTillToday().orElse(BigDecimal.ZERO);
        switch (getDecisionType(responseAcceptation)) {
            case DEFENDANT:
                settlement.makeOffer(prepareOffer(response, paymentIntention, claimAmountTillDate), MadeBy.DEFENDANT);
                break;
            case CLAIMANT:
            case CLAIMANT_IN_FAVOUR_OF_DEFENDANT:
                settlement.makeOffer(prepareOffer(response, paymentIntention, claimAmountTillDate), MadeBy.CLAIMANT);
                break;
            case COURT:
                settlement.makeOffer(prepareOffer(response, paymentIntention, claimAmountTillDate), MadeBy.COURT);
                break;
            default:
                throw new IllegalStateException("Invalid decision type in the Claim");

        }
        settlement.acceptCourtDetermination(MadeBy.CLAIMANT);
        this.offersService.signSettlementAgreement(claim.getExternalId(), settlement, authorisation);
    }

    private DecisionType getDecisionType(ResponseAcceptation responseAcceptation) {
        Optional<CourtDetermination> courtDetermination = responseAcceptation.getCourtDetermination();
        return courtDetermination.map(CourtDetermination::getDecisionType).orElse(DecisionType.DEFENDANT);
    }

    private Offer prepareOffer(Response response, PaymentIntention paymentIntention, BigDecimal claimAmountTillDate) {
        Offer.OfferBuilder builder = Offer.builder();
        builder.paymentIntention(paymentIntention);

        switch (paymentIntention.getPaymentOption()) {
            case IMMEDIATELY:
            case BY_SPECIFIED_DATE:
                LocalDate completionDate = paymentIntention.getPaymentDate().orElseThrow(IllegalStateException::new);
                builder.completionDate(completionDate);
                builder.content(
                    prepareOfferContentsBySetDate(response, completionDate, claimAmountTillDate)
                );
                break;
            case INSTALMENTS:
                RepaymentPlan repaymentPlan = paymentIntention.getRepaymentPlan().orElseThrow(IllegalAccessError::new);
                builder.completionDate(repaymentPlan.getCompletionDate());
                builder.content(
                    prepareOfferContentForRepayment(response, repaymentPlan, claimAmountTillDate)
                );
                break;
            default:
                throw new IllegalStateException("Invalid payment option " + paymentIntention.getPaymentOption());
        }
        return builder.build();
    }

    private String prepareOfferContentForRepayment(Response response, RepaymentPlan repaymentPlan,
                                                   BigDecimal claimAmountTillDate) {
        return String.format(
            "%s will repay %s in instalments of %s %s. The first instalment will be paid by %s.",
            response.getDefendant().getName(),
            response.getResponseType() == ResponseType.PART_ADMISSION
                ? formatMoney(((PartAdmissionResponse) response).getAmount()) : formatMoney(claimAmountTillDate),
            formatMoney(repaymentPlan.getInstalmentAmount()),
            repaymentPlan.getPaymentSchedule().getDescription().toLowerCase(),
            formatDate(repaymentPlan.getFirstPaymentDate()));
    }

    private String prepareOfferContentsBySetDate(
        Response response,
        LocalDate completionDate,
        BigDecimal claimAmountTillDate
    ) {
        String amount;
        switch (response.getResponseType()) {
            case PART_ADMISSION:
                PartAdmissionResponse partAdmissionResponse = (PartAdmissionResponse) response;
                amount = formatMoney(partAdmissionResponse.getAmount());
                break;
            case FULL_ADMISSION:
                amount = formatMoney(claimAmountTillDate);
                break;
            default:
                throw new IllegalStateException("Invalid response type " + response.getResponseType());
        }
        return String.format("%s will pay %s no later than %s",
            response.getDefendant().getName(), amount, formatDate(completionDate)
        );
    }

    private void formaliseCCJ(Claim claim, ResponseAcceptation responseAcceptation, String authorisation) {
        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);
        PaymentIntention acceptedPaymentIntention = acceptedPaymentIntention(responseAcceptation, response);

        CountyCourtJudgment.CountyCourtJudgmentBuilder countyCourtJudgment = CountyCourtJudgment.builder()
            .defendantDateOfBirth(defendantDateOfBirth(response.getDefendant()))
            .paymentOption(acceptedPaymentIntention.getPaymentOption())
            .paidAmount(responseAcceptation.getAmountPaid().orElse(null))
            .repaymentPlan(acceptedPaymentIntention.getRepaymentPlan().orElse(null))
            .payBySetDate(acceptedPaymentIntention.getPaymentDate().orElse(null));

        if (responseAcceptation.getCourtDetermination().isPresent()) {
            countyCourtJudgment.ccjType(CountyCourtJudgmentType.DETERMINATION);
        } else {
            countyCourtJudgment.ccjType(CountyCourtJudgmentType.ADMISSIONS);
        }

        this.countyCourtJudgmentService.save(
            countyCourtJudgment.build(),
            claim.getExternalId(),
            authorisation);
    }

    private LocalDate defendantDateOfBirth(Party party) {
        if (party instanceof Individual) {
            return ((Individual) party).getDateOfBirth();
        }
        return null;
    }

    private PaymentIntention acceptedPaymentIntention(ResponseAcceptation responseAcceptation, Response response) {

        return responseAcceptation.getCourtDetermination()
            .map(CourtDetermination::getCourtDecision)
            .orElseGet(() -> getDefendantPaymentIntention(response));

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
