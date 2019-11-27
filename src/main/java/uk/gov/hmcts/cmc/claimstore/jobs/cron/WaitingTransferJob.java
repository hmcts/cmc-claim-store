package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.StateTransition;

@Getter
@Component
public class WaitingTransferJob extends AbstractStateTransitionJob {

    private final StateTransition stateTransition = StateTransition.WAITING_TRANSFER;

    private final String cronExpression;

    public WaitingTransferJob(@Value("${schedule.state-transition.waiting-transfer}") String cronExpression) {
        this.cronExpression = cronExpression;
    }
}
