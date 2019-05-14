package uk.gov.hmcts.cmc.claimstore.events.claim;

@FunctionalInterface
public interface RepNotificationOperation<C, A, S, R, U> {
    U perform(C claim, A authorisation, S submitterName, R representativeEmail);
}
