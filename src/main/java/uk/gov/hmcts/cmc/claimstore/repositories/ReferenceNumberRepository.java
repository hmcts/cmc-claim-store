package uk.gov.hmcts.cmc.claimstore.repositories;

import org.skife.jdbi.v2.sqlobject.SqlQuery;

public interface ReferenceNumberRepository {

    @SqlQuery("SELECT next_reference_number()")
    String getReferenceNoForCitizen();

    @SqlQuery("SELECT next_legal_rep_reference_number()")
    String getReferenceNoForLegal();

}
