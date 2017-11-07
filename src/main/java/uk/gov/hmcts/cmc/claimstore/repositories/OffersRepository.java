package uk.gov.hmcts.cmc.claimstore.repositories;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface OffersRepository {

    @SqlUpdate(
        "UPDATE claim "
            + "SET partyStatement = :partyStatement::JSONB "
            + "WHERE id = :claimId"
    )
    void updateSettlement(
        @Bind("claimId") Long claimId,
        @Bind("partyStatement") String settlement
    );

}
