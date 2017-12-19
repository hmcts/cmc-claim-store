package uk.gov.hmcts.cmc.claimstore.repositories;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.ClaimMapper;

import java.time.LocalDate;

@RegisterMapper(ClaimMapper.class)
@ConditionalOnProperty("claim-store.test-support.enabled")
@SuppressWarnings("squid:AS1609") // Not a functional interface sonar, delete this if you add another method
public interface TestingSupportRepository {
    @SqlUpdate(
        "UPDATE claim SET response_deadline = :responseDeadline "
            + "WHERE id = :claimId"
    )
    void updateResponseDeadline(
        @Bind("claimId") Long claimId,
        @Bind("responseDeadline") LocalDate responseDeadline
    );
}
