package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import uk.gov.hmcts.cmc.ccd.assertion.StatementOfTruthAssert;
import uk.gov.hmcts.cmc.ccd.assertion.TimelineEventAssert;
import uk.gov.hmcts.cmc.ccd.assertion.response.DefendantEvidenceAssert;
import uk.gov.hmcts.cmc.ccd.assertion.response.DefendantTimelineAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.assertion.response.FullAdmissionResponseAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.assertion.response.FullDefenceResponseAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.assertion.response.PartAdmissionResponseAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.assertion.response.PaymentIntentionAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.assertion.response.ResponseAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.assertion.statementofmeans.BankAccountAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.assertion.statementofmeans.ChildAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.assertion.statementofmeans.CourtOrderAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.assertion.statementofmeans.DebtAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.assertion.statementofmeans.DependantAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.assertion.statementofmeans.EmployerAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.assertion.statementofmeans.EmploymentAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.assertion.statementofmeans.ExpenseAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.assertion.statementofmeans.IncomeAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.assertion.statementofmeans.SelfEmploymentAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.assertion.statementofmeans.StatementOfMeansAssert;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Dependant;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.LivingPartner;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;


public class Assertions {

    private Assertions() {
    }

    public static StatementOfTruthAssert assertThat(StatementOfTruth statementOfTruth) {
        return new StatementOfTruthAssert(statementOfTruth);
    }

    public static RepaymentPlanAssert assertThat(RepaymentPlan repaymentPlan) {
        return new RepaymentPlanAssert(repaymentPlan);
    }

    public static CountyCourtJudgmentAssert assertThat(CountyCourtJudgment countyCourtJudgment) {
        return new CountyCourtJudgmentAssert(countyCourtJudgment);
    }

    public static FullDefenceResponseAssert assertThat(FullDefenceResponse fullDefenceResponse) {
        return new FullDefenceResponseAssert(fullDefenceResponse);
    }

    public static SettlementAssert assertThat(Settlement settlement) {
        return new SettlementAssert(settlement);
    }

    public static PartyStatementAssert assertThat(PartyStatement partyStatement) {
        return new PartyStatementAssert(partyStatement);
    }

    public static OfferAssert assertThat(Offer offer) {
        return new OfferAssert(offer);
    }

    public static PaymentDeclarationAssert assertThat(PaymentDeclaration actual) {
        return new PaymentDeclarationAssert(actual);
    }

    public static TimelineEventAssert assertThat(TimelineEvent timelineEvent) {
        return new TimelineEventAssert(timelineEvent);
    }

    public static EvidenceRowAssert assertThat(EvidenceRow evidenceRow) {
        return new EvidenceRowAssert(evidenceRow);
    }

    public static DefendantTimelineAssert assertThat(DefendantTimeline timeline) {
        return new DefendantTimelineAssert(timeline);
    }

    public static DefendantEvidenceAssert assertThat(DefendantEvidence evidence) {
        return new DefendantEvidenceAssert(evidence);
    }

    public static ResponseAssert assertThat(Response response) {
        return new ResponseAssert(response);
    }

    public static ClaimantResponseAssert assertThat(ClaimantResponse response) {
        return new ClaimantResponseAssert(response);
    }

    public static ResponseRejectionAssert assertThat(ResponseRejection responseRejection) {
        return new ResponseRejectionAssert(responseRejection);
    }

    public static CourtDeterminationAssert assertThat(CourtDetermination courtDetermination) {
        return new CourtDeterminationAssert(courtDetermination);
    }

    public static ResponseAcceptationAssert assertThat(ResponseAcceptation responseAcceptation) {
        return new ResponseAcceptationAssert(responseAcceptation);
    }

    public static FullAdmissionResponseAssert assertThat(FullAdmissionResponse fullAdmissionResponse) {
        return new FullAdmissionResponseAssert(fullAdmissionResponse);
    }

    public static PartAdmissionResponseAssert assertThat(PartAdmissionResponse partAdmissionResponse) {
        return new PartAdmissionResponseAssert(partAdmissionResponse);
    }

    public static BankAccountAssert assertThat(BankAccount bankAccount) {
        return new BankAccountAssert(bankAccount);
    }

    public static ChildAssert assertThat(Child child) {
        return new ChildAssert(child);
    }

    public static CourtOrderAssert assertThat(CourtOrder courtOrder) {
        return new CourtOrderAssert(courtOrder);
    }

    public static DebtAssert assertThat(Debt debt) {
        return new DebtAssert(debt);
    }

    public static DependantAssert assertThat(Dependant dependant) {
        return new DependantAssert(dependant);
    }

    public static EmployerAssert assertThat(Employer employer) {
        return new EmployerAssert(employer);
    }

    public static IncomeAssert assertThat(Income income) {
        return new IncomeAssert(income);
    }

    public static ExpenseAssert assertThat(Expense expense) {
        return new ExpenseAssert(expense);
    }

    public static LivingPartnerAssert assertThat(LivingPartner livingPartner) {
        return new LivingPartnerAssert(livingPartner);
    }

    public static SelfEmploymentAssert assertThat(SelfEmployment selfEmployment) {
        return new SelfEmploymentAssert(selfEmployment);
    }

    public static EmploymentAssert assertThat(Employment employment) {
        return new EmploymentAssert(employment);
    }

    public static StatementOfMeansAssert assertThat(StatementOfMeans statementOfMeans) {
        return new StatementOfMeansAssert(statementOfMeans);
    }

    public static PaymentIntentionAssert assertThat(PaymentIntention paymentIntention) {
        return new PaymentIntentionAssert(paymentIntention);
    }
}
