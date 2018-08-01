package uk.gov.hmcts.cmc.claimstore.repositories;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.hmcts.cmc.domain.models.AuthorizedRole;

import java.util.Optional;

public interface UserAuthorizedRolesRepository {
    @SqlUpdate("INSERT INTO user_authorized_roles ( "
        + "user_id, "
        + "role"
        + ") "
        + "VALUES ("
        + ":userId, "
        + ":role "
        + ")")
    void saveAuthorizedUserRoles(
        @Bind("userId") String userId,
        @Bind("role") String role
    );

    @SingleValueResult
    @SqlQuery("SELECT * FROM user_authorized_roles WHERE claim.user_id = :userId")
    Optional<AuthorizedRole> getByUserId(@Bind("userId") String userId);
}
