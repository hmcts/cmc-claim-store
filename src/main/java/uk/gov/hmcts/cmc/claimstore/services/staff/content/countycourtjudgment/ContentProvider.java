package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.CCJContent;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.RepaymentPlanContent;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Collections;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Component
public class ContentProvider {

    private final InterestCalculationService interestCalculationService;
    private final RepaymentPlanContentProvider repaymentPlanContentProvider;

    @Autowired
    public ContentProvider(InterestCalculationService interestCalculationService, RepaymentPlanContentProvider repaymentPlanContentProvider) {
        this.interestCalculationService = interestCalculationService;
        this.repaymentPlanContentProvider = repaymentPlanContentProvider;
    }

    public Map<String, Object> createContent(Claim claim) {
        requireNonNull(claim);
        return Collections.singletonMap("ccj", new CCJContent(claim, interestCalculationService, repaymentPlanContentProvider));
    }
}
