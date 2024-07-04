package uk.gov.hmcts.cmc.claimstore.api.provider;

import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.AmountRow;
import uk.gov.hmcts.cmc.domain.models.BreathingSpace;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.ProceedOfflineReasonType;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.ResponseMethod;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProviderTestUtils {

    private ProviderTestUtils() {
    }

    public static Claim getClaimResponse() {
        return Claim.builder()
            .submitterId("100")
            .letterHolderId("letter")
            .defendantId("defeId")
            .externalId("extId")
            .referenceNumber("refNum")
            .claimData(ClaimData.builder()
                .amount(AmountBreakDown.builder()
                    .rows(List.of(AmountRow.builder().reason("reason")
                        .amount(new BigDecimal(20)).build())).build())
                .claimants(List.of(Individual.builder()
                    .name("name")
                    .address(getAddressResponse())
                    .correspondenceAddress(getAddressResponse())
                    .build()))
                .breathingSpace(BreathingSpace.builder()
                    .bsReferenceNumber("bsRef")
                    .bsEnteredDate(LocalDate.now())
                    .bsLiftedDate(LocalDate.now())
                    .bsEnteredDateByInsolvencyTeam(LocalDate.now())
                    .bsLiftedDateByInsolvencyTeam(LocalDate.now())
                    .bsExpectedEndDate(LocalDate.now())
                    .bsLiftedFlag("bsFlag")
                    .build())
                .build())
            .responseDeadline(LocalDate.now())
            .moreTimeRequested(true)
            .submitterEmail("sub@email.com")
            .response(PartAdmissionResponse.builder()
                .paymentIntention(getPaymentIntention())
                .paymentDeclaration(PaymentDeclaration.builder().paidDate(LocalDate.now())
                    .paidAmount(new BigDecimal(20)).build())
                .responseMethod(ResponseMethod.OFFLINE)
                .build())
            .moneyReceivedOn(LocalDate.now())
            .countyCourtJudgmentRequestedAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .reDeterminationRequestedAt(LocalDateTime.now())
            .intentionToProceedDeadline(LocalDate.now())
            .claimantRespondedAt(LocalDateTime.now())
            .claimantResponse(ResponseAcceptation.builder()
                .amountPaid(new BigDecimal(30))
                .paymentReceived(YesNoOption.YES)
                .settleForAmount(YesNoOption.YES)
                .courtDetermination(CourtDetermination.builder()
                    .courtDecision(getPaymentIntention())
                    .courtPaymentIntention(getPaymentIntention())
                    .rejectionReason("rjctReason")
                    .disposableIncome(new BigDecimal(30))
                    .build())
                .formaliseOption(FormaliseOption.SETTLEMENT)
                .build())
            .state(ClaimState.OPEN)
            .proceedOfflineReason(ProceedOfflineReasonType.OTHER)
            .settlement(Settlement.builder()
                .partyStatements(List.of(PartyStatement.builder()
                    .type(StatementType.OFFER)
                    .madeBy(MadeBy.CLAIMANT)
                    .offer(Offer.builder().content("content").completionDate(LocalDate.now())
                        .paymentIntention(getPaymentIntention()).build())
                    .build()))
                .build())
            .build();
    }

    private static PaymentIntention getPaymentIntention() {
        return PaymentIntention.builder().paymentOption(PaymentOption.IMMEDIATELY)
            .paymentDate(LocalDate.now()).build();
    }

    private static Address getAddressResponse() {
        return Address.builder().line1("line1")
            .line2("line2")
            .line3("line3")
            .city("city")
            .county("county")
            .postcode("postcode")
            .build();
    }
}
