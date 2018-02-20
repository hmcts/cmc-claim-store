package uk.gov.hmcts.cmc.claimstore.repositories;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.time.LocalDateTime;

public interface OffersRepository {

    @SqlUpdate(
        "UPDATE claim "
            + " SET settlement = :settlement::JSONB "
            + " WHERE external_id = :externalId"
    )
    void updateSettlement(
        @Bind("externalId") String externalId,
        @Bind("settlement") String settlement
    );

    @SqlUpdate(
        "UPDATE claim "
            + " SET "
            + " settlement = :settlement::JSONB, settlement_reached_at = :settlementReachedAt "
            + " WHERE external_id = :externalId"
    )
    void reachSettlement(
        @Bind("externalId") String externalId,
        @Bind("settlement") String settlement,
        @Bind("settlementReachedAt") LocalDateTime settlementReachedAt
    );
}
