package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.DisabilityStatus;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.LivingPartner;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PriorityDebt;
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

    private List<CCDCollectionElement<PriorityDebt>> priorityDebts;
    private CCDYesNoOption carer;
    private DisabilityStatus disabilityStatus;
    private LivingPartner livingPartner;

    private CCDEmploymentStatus employmentStatus; //TODO: Not Needed
}
