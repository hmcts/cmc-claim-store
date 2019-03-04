package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class CCDStatementOfMeans {
    private String reason;
    private CCDResidenceType residenceType;
    private String residenceOtherDetail;
    private Integer noOfMaintainedChildren;
    private List<CCDCollectionElement<CCDChildCategory>> dependantChildren;
    private CCDYesNoOption anyDisabledChildren;
    private Integer numberOfOtherDependants;
    private String otherDependantDetails;
    private CCDYesNoOption otherDependantAnyDisabled;
    private List<CCDCollectionElement<CCDEmployer>> employers;
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
    private List<CCDCollectionElement<CCDPriorityDebt>> priorityDebts;
    private CCDYesNoOption carer;
    private CCDDisabilityStatus disabilityStatus;
    private CCDLivingPartner livingPartner;
    private CCDYesNoOption retired;
}
