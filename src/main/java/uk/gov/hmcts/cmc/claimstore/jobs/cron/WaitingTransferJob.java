package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.statetransition.StateTransition;
import uk.gov.hmcts.cmc.claimstore.services.statetransition.StateTransitions;

@Getter
@Component
public class WaitingTransferJob extends AbstractStateTransitionJob {

    private final StateTransition stateTransition = StateTransitions.WAITING_TRANSFER;

    private String cronExpression;

    public WaitingTransferJob() {
    }

    //Cannot autowire via constructor as job framework needs a zero argument constructor
    @Autowired
    public void setCronExpression(@Value("${schedule.state-transition.waiting-transfer}") String cronExpression) {
        this.cronExpression = cronExpression;
    }
}
