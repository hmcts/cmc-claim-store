package uk.gov.hmcts.cmc.claimstore.events.claim;

@FunctionalInterface
public interface NotificationOperation<C, A, S, U> {
    U perform(C claim, A authorisation, S submitterName);
}
