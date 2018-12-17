package uk.gov.hmcts.cmc.ccd.domain.defendant;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDCourtOrder;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDDebt;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDIncome;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Residence;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Value
public class CCDStatementOfMeans {
    private String reason;
    private Residence.ResidenceType residenceType;
    private String residenceOtherDetail;
    private Integer noOfMaintainedChildren;
    private List<CCDCollectionElement<CCDChildCategory>> dependantChildren;
    private CCDYesNoOption anyDisabledChildren;
    private Integer numberOfOtherDependants;
    private String otherDependantDetails;
    private CCDEmploymentStatus employmentStatus;
    private List<CCDCollectionElement<CCDEmployment>> employers;
    private String taxPaymentsReason;
    private BigDecimal taxYouOwe;
    private String selfEmploymentJobTitle;
    private BigDecimal selfEmploymentAnnualTurnover;
    private Integer unEmployedNoOfYears;
    private Integer unEmployedNoOfMonths;
    private String employmentDetails;
    private List<CCDCollectionElement<CCDBankAccount>> bankAccounts;
    private List<CCDCollectionElement<CCDDebt>> debts;
    private List<CCDCollectionElement<CCDIncome>> incomes;
    private List<CCDCollectionElement<CCDExpense>> expenses;
    private List<CCDCollectionElement<CCDCourtOrder>> courtOrders;
}
