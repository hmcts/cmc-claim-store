package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimDataContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Dependant;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.OnTaxPayments;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Residence;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDateTime;

@Component
public class StatementOfMeansContentProvider {

    private final PartyDetailsContentProvider partyDetailsContentProvider;
    private final ClaimDataContentProvider claimDataContentProvider;
    private final NotificationsProperties notificationsProperties;

    public StatementOfMeansContentProvider(
        PartyDetailsContentProvider partyDetailsContentProvider,
        ClaimDataContentProvider claimDataContentProvider,
        NotificationsProperties notificationsProperties
    ) {
        this.partyDetailsContentProvider = partyDetailsContentProvider;
        this.claimDataContentProvider = claimDataContentProvider;
        this.notificationsProperties = notificationsProperties;
    }

    public Map<String, Object> createContent(Claim claim) {
        requireNonNull(claim);
        Response defendantResponse = claim.getResponse().orElseThrow(IllegalStateException::new);

        Map<String, Object> content = new HashMap<>();

        Optional<StatementOfTruth> optionalStatementOfTruth = defendantResponse.getStatementOfTruth();
        content.put("signerName", optionalStatementOfTruth.map((StatementOfTruth::getSignerName)).orElse(null));
        content.put("signerRole", optionalStatementOfTruth.map((StatementOfTruth::getSignerRole)).orElse(null));

        content.put("claim", claimDataContentProvider.createContent(claim));
        content.put("defenceSubmittedOn", formatDateTime(claim.getRespondedAt()));
        content.put("defenceSubmittedDate", formatDate(claim.getRespondedAt()));
        content.put("responseDashboardUrl", notificationsProperties.getFrontendBaseUrl());

        content.put("defendant", partyDetailsContentProvider.createContent(
            claim.getClaimData().getDefendant(),
            defendantResponse.getDefendant(),
            claim.getDefendantEmail(),
            null,
            null
        ));
        content.put("claimant", partyDetailsContentProvider.createContent(
            claim.getClaimData().getClaimant(),
            claim.getSubmitterEmail()
        ));

        List<BankAccount> bankAccounts = null;
        List<CourtOrder> courtOrders = null;
        List<Expense> expenses = null;
        List<Income> incomes = null;
        List<Child> children = null;
        List<Debt> debts = null;

        Residence residence = null;
        RepaymentPlan repaymentPlan = null;
        StatementOfMeans statementOfMeans;
        Employment employment;
        SelfEmployment selfEmployment;
        OnTaxPayments onTaxPayments = null;
        Integer maintainedChildren = null;

        if (defendantResponse instanceof FullAdmissionResponse) {
            FullAdmissionResponse fullAdmissionResponse = (FullAdmissionResponse) defendantResponse;
            content.put("paymentOption", fullAdmissionResponse.getPaymentOption().getDescription());

            Optional<RepaymentPlan> optionalRepaymentPlan = fullAdmissionResponse.getRepaymentPlan();
            if (optionalRepaymentPlan.isPresent()){
                repaymentPlan = optionalRepaymentPlan.get();
                content.put("repaymentPlan", repaymentPlan);
            }

            Optional<StatementOfMeans> optionalStatementOfMeans = fullAdmissionResponse.getStatementOfMeans();
            if (optionalStatementOfMeans.isPresent()) {
                statementOfMeans = optionalStatementOfMeans.get();
                residence = statementOfMeans.getResidence();
                content.put("residence", residence);

                Optional<Dependant> optionalDependant = statementOfMeans.getDependant();
                if (optionalDependant.isPresent()) {
                    Dependant dependant = optionalDependant.get();
                    children = dependant.getChildren();
                    content.put("children", children);
                    Optional<Integer> optionalMaintainedChildren = dependant.getNumberOfMaintainedChildren();
                    maintainedChildren = optionalMaintainedChildren.get();
                    content.put("maintainedChildren", maintainedChildren);
                }
                bankAccounts = statementOfMeans.getBankAccounts();
                courtOrders = statementOfMeans.getCourtOrders();
                expenses = statementOfMeans.getExpenses();
                incomes = statementOfMeans.getIncomes();
                debts = statementOfMeans.getDebts();

                Optional<Employment> optionalEmployment = statementOfMeans.getEmployment();
                if(optionalEmployment.isPresent()){
                    employment = optionalEmployment.get();
                    if (employment.getSelfEmployment().isPresent()){
                        Optional<SelfEmployment> optionalSelfEmployment = employment.getSelfEmployment();
                        if(optionalSelfEmployment.isPresent()){
                            selfEmployment = optionalSelfEmployment.get();
                            Optional<OnTaxPayments> optionalTaxPayments = selfEmployment.getOnTaxPayments();
                            onTaxPayments = optionalTaxPayments.get();
                            content.put("onTaxPayments", onTaxPayments);
                        }
                    }
                }
            }

        }
        content.put("bankAccounts", bankAccounts);
        content.put("expenses", expenses);
        content.put("incomes", incomes);
        content.put("courtOrders", courtOrders);
        content.put("debts", debts);
        return  content;
    }
}
