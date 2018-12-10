package uk.gov.hmcts.cmc.ccd.jackson.mixin;


import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import java.util.Optional;

public interface CountryCourtJudgmentMixin {

    @JsonUnwrapped(prefix = "rp")
    Optional<RepaymentPlan> getRepaymentPlan();

    @JsonUnwrapped
    Optional<StatementOfTruth> getStatementOfTruth();

}
