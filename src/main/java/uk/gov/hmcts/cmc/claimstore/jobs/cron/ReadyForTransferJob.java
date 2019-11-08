package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.cmc.claimstore.services.StateTransition;

@Getter
public class ReadyForTransferJob extends AbstractStateTransitionJob {

    private final StateTransition stateTransition = StateTransition.WAITING_TRANSFER;

    @Value("${stateTransition.waitingTransfer}")
    private String cronExpression;
}
