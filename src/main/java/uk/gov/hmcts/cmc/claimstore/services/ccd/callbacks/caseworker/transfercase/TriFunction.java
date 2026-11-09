package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

/**
 * Three-argument function used while handing a case to another court.
 */
@FunctionalInterface
public interface TriFunction<S, T, U, R> {

    R apply(S first, T second, U third);

}
