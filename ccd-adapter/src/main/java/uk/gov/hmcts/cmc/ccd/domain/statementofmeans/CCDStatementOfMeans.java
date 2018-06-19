package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;

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
    private String reason;
}
