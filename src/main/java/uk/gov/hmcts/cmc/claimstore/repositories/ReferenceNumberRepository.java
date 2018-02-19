package uk.gov.hmcts.cmc.claimstore.repositories;

import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import org.springframework.stereotype.Repository;

@Repository("referenceNumberRepository")
public interface ReferenceNumberRepository {

    @SingleValueResult
    @SqlQuery("SELECT next_reference_number()")
    String getReferenceNoForCitizen();

    @SingleValueResult
    @SqlQuery("SELECT next_legal_rep_reference_number()")
    String getReferenceNoForLegal();

}
