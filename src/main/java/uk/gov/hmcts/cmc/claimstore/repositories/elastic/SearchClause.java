package uk.gov.hmcts.cmc.claimstore.repositories.elastic;

/**
 * One clause of the CCD search DSL. {@link #toString()} is the JSON sent to CCD.
 */
public interface SearchClause {

    void write(SearchJson json);

}
