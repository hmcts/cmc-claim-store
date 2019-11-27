package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.StateTransition;

@Getter
@Component
public class StayClaimJob extends AbstractStateTransitionJob {

    private final StateTransition stateTransition = StateTransition.STAY_CLAIM;

    private final String cronExpression;

    public StayClaimJob(@Value("${schedule.state-transition.stay-claim}") String cronExpression) {
        this.cronExpression = cronExpression;
    }
}
