package uk.gov.hmcts.cmc.claimstore.events.claim;

@FunctionalInterface
public interface RepNotificationOperation<C, E, U> {
    U perform(C claim, E event);
}
