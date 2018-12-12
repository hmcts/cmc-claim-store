package uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.DisabilityStatus;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PriorityDebt;

import java.util.List;

@Builder
@Value
public class CCDStatementOfMeans {
    private CCDResidence residence;
    private CCDDependant dependant;
    private CCDEmployment employment;
    private List<CCDCollectionElement<CCDBankAccount>> bankAccounts;
    private List<CCDCollectionElement<CCDDebt>> debts;
    private List<CCDCollectionElement<CCDIncome>> incomes;
    private List<CCDCollectionElement<CCDExpense>> expenses;
    private List<CCDCollectionElement<CCDCourtOrder>> courtOrders;
    private List<CCDCollectionElement<PriorityDebt>> priorityDebts;
    private CCDLivingPartner partner;
    private DisabilityStatus disability;
    private CCDYesNoOption carer;
    private String reason;
}
