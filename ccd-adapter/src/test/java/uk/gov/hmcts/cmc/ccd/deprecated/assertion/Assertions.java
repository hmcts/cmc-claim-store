package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import uk.gov.hmcts.cmc.ccd.assertion.EvidenceRowAssert;
import uk.gov.hmcts.cmc.ccd.assertion.TimelineEventAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.BankAccountAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.ChildAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.CourtOrderAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.EmployerAssert;
import uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans.LivingPartnerAssert;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.LivingPartner;

public class Assertions {

    private Assertions() {
    }

    public static OfferAssert assertThat(Offer offer) {
        return new OfferAssert(offer);
    }

    public static TimelineEventAssert assertThat(TimelineEvent timelineEvent) {
        return new TimelineEventAssert(timelineEvent);
    }

    public static EvidenceRowAssert assertThat(EvidenceRow evidenceRow) {
        return new EvidenceRowAssert(evidenceRow);
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

    public static EmployerAssert assertThat(Employer employer) {
        return new EmployerAssert(employer);
    }

    public static LivingPartnerAssert assertThat(LivingPartner livingPartner) {
        return new LivingPartnerAssert(livingPartner);
    }
}
