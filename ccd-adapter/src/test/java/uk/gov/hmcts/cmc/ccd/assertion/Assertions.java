package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.ccd.domain.CCDAddress;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactDetails;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterest;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestDate;
import uk.gov.hmcts.cmc.ccd.domain.CCDPayment;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentState;
import uk.gov.hmcts.cmc.ccd.domain.CCDRepresentative;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.PaymentState;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;
import uk.gov.hmcts.cmc.domain.models.ccj.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

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

    public static PaymentStateAssert assertThat(PaymentState paymentState) {
        return new PaymentStateAssert(paymentState);
    }

    public static CCDPaymentStateAssert assertThat(CCDPaymentState ccdPaymentState) {
        return new CCDPaymentStateAssert(ccdPaymentState);
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

    public static ResponseAssert assertThat(FullDefenceResponse fullDefenceResponse) {
        return new ResponseAssert(fullDefenceResponse);
    }
}
