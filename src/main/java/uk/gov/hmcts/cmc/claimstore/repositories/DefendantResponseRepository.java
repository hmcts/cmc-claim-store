package uk.gov.hmcts.cmc.claimstore.repositories;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.hmcts.cmc.claimstore.models.DefendantResponse;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.DefendantResponseMapper;

import java.util.List;
import java.util.Optional;

@RegisterMapper(DefendantResponseMapper.class)
public interface DefendantResponseRepository {

    @SingleValueResult
    @SqlQuery("SELECT * FROM defendant_response WHERE defendant_id = :defendantId")
    List<DefendantResponse> getByDefendantId(@Bind("defendantId") Long defendantId);

    @SingleValueResult
    @SqlQuery("SELECT * FROM defendant_response WHERE claim_id = :claimId")
    Optional<DefendantResponse> getByClaimId(@Bind("claimId") Long claimId);

    @SingleValueResult
    @SqlQuery("SELECT * FROM defendant_response WHERE id = :id")
    Optional<DefendantResponse> getById(@Bind("id") final Long id);

    @GetGeneratedKeys
    @SqlUpdate(
        "INSERT INTO defendant_response ( "
            + "claim_id, "
            + "defendant_id, "
            + "defendant_email, "
            + "response "
            + ") "
            + "VALUES ("
            + ":claimId, "
            + ":defendantId, "
            + ":defendantEmail, "
            + ":response::JSONB "
            + ")"
    )
    Long save(
        @Bind("claimId") final Long claimId,
        @Bind("defendantId") final Long defendantId,
        @Bind("defendantEmail") final String defendantEmail,
        @Bind("response") final String response
    );
}
