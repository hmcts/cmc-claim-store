package uk.gov.hmcts.cmc.claimstore.repositories;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.hmcts.cmc.claimstore.models.DefaultJudgment;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.DefaultJudgmentMapper;

import java.util.Optional;

@RegisterMapper(DefaultJudgmentMapper.class)
public interface DefaultJudgmentRepository {

    @SingleValueResult
    @SqlQuery("SELECT * FROM default_judgment WHERE id = :defaultJudgmentId")
    Optional<DefaultJudgment> getById(@Bind("defaultJudgmentId") Long defaultJudgmentId);

    @SingleValueResult
    @SqlQuery("SELECT * FROM default_judgment WHERE claim_id = :claimId")
    Optional<DefaultJudgment> getByClaimId(@Bind("claimId") Long claimId);

    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO default_judgment ( "
        + "claim_id, "
        + "claimant_id, "
        + "external_id, "
        + "data "
        + ") "
        + "VALUES ("
        + ":claimant_id, "
        + ":claim_id, "
        + ":external_id, "
        + ":data::JSONB "
        + ")")
    Long save(
        @Bind("claim_id") final Long claimId,
        @Bind("claimant_id") final Long claimantId,
        @Bind("external_id") final String externalId,
        @Bind("data") final String data
    );
}
