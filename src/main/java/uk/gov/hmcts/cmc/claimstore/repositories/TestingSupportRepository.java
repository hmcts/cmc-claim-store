package uk.gov.hmcts.cmc.claimstore.repositories;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.ClaimMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.util.Optional;

@RegisterMapper(ClaimMapper.class)
@ConditionalOnProperty("claim-store.test-support.enabled")
@SuppressWarnings("squid:AS1609") // Not a functional interface sonar, delete this if you add another method
public interface TestingSupportRepository {
    @SqlUpdate(
        "UPDATE claim SET response_deadline = :responseDeadline "
            + "WHERE external_id = :externalId"
    )
    void updateResponseDeadline(
        @Bind("externalId") String externalId,
        @Bind("responseDeadline") LocalDate responseDeadline
    );

    @SingleValueResult
    @SqlQuery("SELECT * FROM claim WHERE claim.reference_number = :claimReferenceNumber")
    Optional<Claim> getByClaimReferenceNumber(@Bind("claimReferenceNumber") String claimReferenceNumber);

    @SqlUpdate(
        "UPDATE claim SET defendant_id = :linkID "
            + "WHERE external_id = :externalId"
    )
    void updateDefendantId(
        @Bind("externalId") String externalId,
        @Bind("linkID") String defendantId
    );
}
