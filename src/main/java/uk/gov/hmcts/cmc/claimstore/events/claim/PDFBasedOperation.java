package uk.gov.hmcts.cmc.claimstore.events.claim;

@FunctionalInterface
public interface PDFBasedOperation<C, A, P, U> {
    U perform(C claim, A authorisation, P pdf);
}
