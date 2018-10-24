package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.ccd.assertion.claimantresponse.ClaimantResponseAssert;
import uk.gov.hmcts.cmc.ccd.assertion.claimantresponse.CourtDeterminationAssert;
import uk.gov.hmcts.cmc.ccd.assertion.claimantresponse.ResponseAcceptationAssert;
import uk.gov.hmcts.cmc.ccd.assertion.claimantresponse.ResponseRejectionAssert;
import uk.gov.hmcts.cmc.ccd.assertion.response.FullAdmissionResponseAssert;
import uk.gov.hmcts.cmc.ccd.assertion.response.FullDefenceResponseAssert;
import uk.gov.hmcts.cmc.ccd.assertion.response.PartAdmissionResponseAssert;
import uk.gov.hmcts.cmc.ccd.assertion.response.PaymentIntentionAssert;
import uk.gov.hmcts.cmc.ccd.assertion.response.ResponseAssert;
import uk.gov.hmcts.cmc.ccd.assertion.statementofmeans.BankAccountAssert;
import uk.gov.hmcts.cmc.ccd.assertion.statementofmeans.ChildAssert;
import uk.gov.hmcts.cmc.ccd.assertion.statementofmeans.CourtOrderAssert;
import uk.gov.hmcts.cmc.ccd.assertion.statementofmeans.DebtAssert;
import uk.gov.hmcts.cmc.ccd.assertion.statementofmeans.DependantAssert;
import uk.gov.hmcts.cmc.ccd.assertion.statementofmeans.EmployerAssert;
import uk.gov.hmcts.cmc.ccd.assertion.statementofmeans.EmploymentAssert;
import uk.gov.hmcts.cmc.ccd.assertion.statementofmeans.ExpenseAssert;
import uk.gov.hmcts.cmc.ccd.assertion.statementofmeans.IncomeAssert;
import uk.gov.hmcts.cmc.ccd.assertion.statementofmeans.SelfEmploymentAssert;
import uk.gov.hmcts.cmc.ccd.assertion.statementofmeans.StatementOfMeansAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterest;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestDate;
import uk.gov.hmcts.cmc.ccd.domain.CCDPayment;
import uk.gov.hmcts.cmc.ccd.domain.CCDRepresentative;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.Timeline;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;
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

    public static AddressAssert assertThat(Address address) {
        return new AddressAssert(address);
    }

    public static InterestAssert assertThat(Interest interest) {
        return new InterestAssert(interest);
    }

    public static InterestDateAssert assertThat(InterestDate interestDate) {
        return new InterestDateAssert(interestDate);
    }

    public static PaymentAssert assertThat(Payment payment) {
        return new PaymentAssert(payment);
    }

    public static CCDAddressAssert assertThat(CCDAddress ccdAddress) {
        return new CCDAddressAssert(ccdAddress);
    }

    public static CCDInterestAssert assertThat(CCDInterest ccdInterest) {
        return new CCDInterestAssert(ccdInterest);
    }

    public static CCDPaymentAssert assertThat(CCDPayment ccdPayment) {
        return new CCDPaymentAssert(ccdPayment);
    }

    public static CCDInterestDateAssert assertThat(CCDInterestDate ccdInterestDate) {
        return new CCDInterestDateAssert(ccdInterestDate);
    }

    public static ContactDetailsAssert assertThat(ContactDetails contactDetails) {
        return new ContactDetailsAssert(contactDetails);
    }

    public static CCDContactDetailsAssert assertThat(CCDContactDetails ccdContactDetails) {
        return new CCDContactDetailsAssert(ccdContactDetails);
    }

    public static RepresentativeAssert assertThat(CCDRepresentative ccdRepresentative) {
        return new RepresentativeAssert(ccdRepresentative);
    }

    public static IndividualAssert assertThat(Individual individual) {
        return new IndividualAssert(individual);
    }

    public static SoleTraderAssert assertThat(SoleTrader soleTrader) {
        return new SoleTraderAssert(soleTrader);
    }

    public static OrganisationAssert assertThat(Organisation organisation) {
        return new OrganisationAssert(organisation);
    }

    public static CompanyAssert assertThat(Company company) {
        return new CompanyAssert(company);
    }

    public static PartyAssert assertThat(Party party) {
        return new PartyAssert(party);
    }


    public static TheirDetailsAssert assertThat(TheirDetails party) {
        return new TheirDetailsAssert(party);
    }

    public static ClaimDataAssert assertThat(ClaimData claimData) {
        return new ClaimDataAssert(claimData);
    }

    public static CompanyDetailsAssert assertThat(CompanyDetails companyDetails) {
        return new CompanyDetailsAssert(companyDetails);
    }

    public static OrganisationDetailsAssert assertThat(OrganisationDetails organisationDetails) {
        return new OrganisationDetailsAssert(organisationDetails);
    }

    public static IndividualDetailsAssert assertThat(IndividualDetails individualDetails) {
        return new IndividualDetailsAssert(individualDetails);
    }

    public static SoleTraderDetailsAssert assertThat(SoleTraderDetails soleTraderDetails) {
        return new SoleTraderDetailsAssert(soleTraderDetails);
    }

    public static AmountRangeAssert assertThat(AmountRange amountRange) {
        return new AmountRangeAssert(amountRange);
    }

    public static AmountAssert assertThat(Amount amount) {
        return new AmountAssert(amount);
    }

    public static StatementOfTruthAssert assertThat(StatementOfTruth statementOfTruth) {
        return new StatementOfTruthAssert(statementOfTruth);
    }

    public static ClaimAssert assertThat(Claim claim) {
        return new ClaimAssert(claim);
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

    public static TimelineAssert assertThat(Timeline timeline) {
        return new TimelineAssert(timeline);
    }

    public static EvidenceRowAssert assertThat(EvidenceRow evidenceRow) {
        return new EvidenceRowAssert(evidenceRow);
    }

    public static EvidenceAssert assertThat(Evidence evidence) {
        return new EvidenceAssert(evidence);
    }

    public static InterestBreakdownAssert assertThat(InterestBreakdown interestBreakdown) {
        return new InterestBreakdownAssert(interestBreakdown);
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
