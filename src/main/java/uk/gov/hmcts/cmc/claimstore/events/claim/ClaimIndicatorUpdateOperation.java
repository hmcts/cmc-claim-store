package uk.gov.hmcts.cmc.claimstore.events.claim;

@FunctionalInterface
public interface ClaimIndicatorUpdateOperation <A,C, E, U> {
    U perform(A authorisation,C claim, E event);
}
