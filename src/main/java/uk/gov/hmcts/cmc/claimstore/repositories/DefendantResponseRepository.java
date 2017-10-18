package uk.gov.hmcts.cmc.claimstore.repositories;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface DefendantResponseRepository {

    @SqlUpdate(
        "UPDATE CLAIM SET "
            + "response = :response::JSONB, "
            + "defendant_id = :defendantId, "
            + "defendant_email = :defendantEmail, "
            + "responded_at = now() AT TIME ZONE 'utc' "
            + "WHERE id = :claimId"
    )
    void save(
        @Bind("claimId") final Long claimId,
        @Bind("defendantId") final String defendantId,
        @Bind("defendantEmail") final String defendantEmail,
        @Bind("response") final String response
    );
}
