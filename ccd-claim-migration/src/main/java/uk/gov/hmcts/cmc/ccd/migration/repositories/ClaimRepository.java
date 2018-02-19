package uk.gov.hmcts.cmc.ccd.migration.repositories;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import uk.gov.hmcts.cmc.ccd.migration.mappers.ClaimMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;

@RegisterMapper(ClaimMapper.class)
@SuppressWarnings("squid:S1214") // Pointless to create class for string statement
public interface ClaimRepository {

    @SqlQuery("SELECT * FROM claim WHERE is_migrated = false")
    List<Claim> getAllNotMigratedClaims();

    @SqlUpdate("UPDATE claim SET is_migrated=true WHERE id=:claimId")
    void markAsMigrated(@Bind("claimId") Long claimId);

}
