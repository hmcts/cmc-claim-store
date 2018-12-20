package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.BankAccountAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.ChildAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.CourtOrderAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.DebtAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.ExpenseAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.IncomeAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.LivingPartnerAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.StatementOfMeansAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.LivingPartner;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;


public class Assertions {

    private Assertions() {
    }

    public static PartyStatementAssert assertThat(PartyStatement partyStatement) {
        return new PartyStatementAssert(partyStatement);
    }

    public static PaymentIntentionAssert assertThat(PaymentIntention paymentIntention) {
        return new PaymentIntentionAssert(paymentIntention);
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

    public static ClaimAssert assertThat(Claim claim) {
        return new ClaimAssert(claim);
    }

    public static ClaimantAssert assertThat(Party party) {
        return new ClaimantAssert(party);
    }

    public static DefendantAssert assertThat(TheirDetails theirDetails) {
        return new DefendantAssert(theirDetails);
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

    public static StatementOfMeansAssert assertThat(StatementOfMeans statementOfMeans) {
        return new StatementOfMeansAssert(statementOfMeans);
    }

    public static LivingPartnerAssert assertThat(LivingPartner livingPartner) {
        return new LivingPartnerAssert(livingPartner);
    }
}
