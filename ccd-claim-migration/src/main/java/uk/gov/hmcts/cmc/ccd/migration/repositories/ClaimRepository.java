package uk.gov.hmcts.cmc.ccd.migration.repositories;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;
import uk.gov.hmcts.cmc.ccd.migration.mappers.ClaimMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;

@RegisterMapper(ClaimMapper.class)
@SuppressWarnings("squid:S1214") // Pointless to create class for string statement
@UseStringTemplate3StatementLocator
public interface ClaimRepository {

    @SqlQuery("SELECT * FROM claim WHERE is_migrated = false order by id desc limit 100")
    List<Claim> getAllNotMigratedClaims();

    @SqlQuery("SELECT * FROM claim WHERE reference_number in (<references>) order by id asc")
    List<Claim> getClaims(@BindIn("references") List<String> references);

    @SqlUpdate("UPDATE claim SET is_migrated=true WHERE id=:claimId")
    void markAsMigrated(@Bind("claimId") Long claimId);

}
