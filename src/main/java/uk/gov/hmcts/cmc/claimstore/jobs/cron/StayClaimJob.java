package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.statetransition.StateTransition;
import uk.gov.hmcts.cmc.claimstore.services.statetransition.StateTransitions;

@Getter
@Component
public class StayClaimJob extends AbstractStateTransitionJob {

    private final StateTransition stateTransition = StateTransitions.STAY_CLAIM;

    private String cronExpression;

    public StayClaimJob() {
    }

    //Cannot autowire via constructor as job framework needs a zero argument constructor
    @Autowired
    public void setCronExpression(@Value("${schedule.state-transition.stay-claim}") String cronExpression) {
        this.cronExpression = cronExpression;
    }
}
