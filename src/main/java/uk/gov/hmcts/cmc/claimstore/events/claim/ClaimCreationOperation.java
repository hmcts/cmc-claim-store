package uk.gov.hmcts.cmc.claimstore.events.claim;

@FunctionalInterface
public interface ClaimCreationOperation<C, E, G, U> {
    U perform(C claim, E event, G docs);
}
