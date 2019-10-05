package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.ccd.assertion.claimantresponse.CourtDeterminationAssert;
import uk.gov.hmcts.cmc.ccd.assertion.claimantresponse.ResponseAcceptationAssert;
import uk.gov.hmcts.cmc.ccd.assertion.claimantresponse.ResponseRejectionAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.PaymentIntentionAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.ResponseAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.BankAccountAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.ChildAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.CourtOrderAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.DebtAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.EmployerAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.ExpenseAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.IncomeAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.LivingPartnerAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.PriorityDebtAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.StatementOfMeansAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDPartyStatement;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReport;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.orders.DirectionOrder;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.LivingPartner;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PriorityDebt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.math.BigDecimal;

public class Assertions {

    private Assertions() {
    }

    public static PartyStatementAssert assertThat(PartyStatement partyStatement) {
        return new PartyStatementAssert(partyStatement);
    }

    public static CCDPartyStatementAssert assertThat(CCDPartyStatement ccdPartyStatement) {
        return new CCDPartyStatementAssert(ccdPartyStatement);
    }

    public static CountyCourtJudgmentAssert assertThat(CountyCourtJudgment countyCourtJudgment) {
        return new CountyCourtJudgmentAssert(countyCourtJudgment);
    }

    public static PaymentIntentionAssert assertThat(PaymentIntention paymentIntention) {
        return new PaymentIntentionAssert(paymentIntention);
    }

    public static CourtDeterminationAssert assertThat(CourtDetermination courtDetermination) {
        return new CourtDeterminationAssert(courtDetermination);
    }

    public static ResponseRejectionAssert assertThat(ResponseRejection responseRejection) {
        return new ResponseRejectionAssert(responseRejection);
    }

    public static ResponseAcceptationAssert assertThat(ResponseAcceptation responseAcceptation) {
        return new ResponseAcceptationAssert(responseAcceptation);
    }

    public static AddressAssert assertThat(Address address) {
        return new AddressAssert(address);
    }

    public static CCDAddressAssert assertThat(CCDAddress ccdAddress) {
        return new CCDAddressAssert(ccdAddress);
    }

    public static TimelineEventAssert assertThat(TimelineEvent timelineEvent) {
        return new TimelineEventAssert(timelineEvent);
    }

    public static EvidenceRowAssert assertThat(EvidenceRow evidenceRow) {
        return new EvidenceRowAssert(evidenceRow);
    }

    public static ExpertReportAssert assertThat(ExpertReport expertReportRow) {
        return new ExpertReportAssert(expertReportRow);
    }

    public static DirectionsQuestionnaireAssert assertThat(DirectionsQuestionnaire directionsQuestionnaire) {
        return new DirectionsQuestionnaireAssert(directionsQuestionnaire);
    }

    public static ClaimAssert assertThat(Claim claim) {
        return new ClaimAssert(claim);
    }

    public static ClaimantAssert assertThat(Party party) {
        return new ClaimantAssert(party);
    }

    public static TheirDetailsAssert assertThat(TheirDetails theirDetails) {
        return new TheirDetailsAssert(theirDetails);
    }

    public static ChildAssert assertThat(Child child) {
        return new ChildAssert(child);
    }

    public static BankAccountAssert assertThat(BankAccount bankAccount) {
        return new BankAccountAssert(bankAccount);
    }

    public static CourtOrderAssert assertThat(CourtOrder courtOrder) {
        return new CourtOrderAssert(courtOrder);
    }

    public static DebtAssert assertThat(Debt debt) {
        return new DebtAssert(debt);
    }

    public static IncomeAssert assertThat(Income income) {
        return new IncomeAssert(income);
    }

    public static ExpenseAssert assertThat(Expense expense) {
        return new ExpenseAssert(expense);
    }

    public static PriorityDebtAssert assertThat(PriorityDebt priorityDebt) {
        return new PriorityDebtAssert(priorityDebt);
    }

    public static StatementOfMeansAssert assertThat(StatementOfMeans statementOfMeans) {
        return new StatementOfMeansAssert(statementOfMeans);
    }

    public static LivingPartnerAssert assertThat(LivingPartner livingPartner) {
        return new LivingPartnerAssert(livingPartner);
    }

    public static ResponseAssert assertThat(Response response) {
        return new ResponseAssert(response);
    }

    public static EmployerAssert assertThat(Employer employer) {
        return new EmployerAssert(employer);
    }

    public static ReviewOrderAssert assertThat(ReviewOrder reviewOrder) {
        return new ReviewOrderAssert(reviewOrder);
    }

    public static DirectionOrderAssert assertThat(DirectionOrder directionOrder) {
        return new DirectionOrderAssert(directionOrder);
    }

    public static MoneyAssert assertMoney(BigDecimal amount) {
        return new MoneyAssert(amount);
    }

}
