package uk.gov.hmcts.cmc.claimstore.repositories;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.UserRolesMapper;
import uk.gov.hmcts.cmc.domain.models.UserRole;

import java.util.List;

@RegisterMapper(UserRolesMapper.class)
public interface UserRolesRepository {
    @SqlUpdate("INSERT INTO user_roles ( "
        + "user_id, "
        + "role"
        + ") "
        + "VALUES ("
        + ":userId, "
        + ":role "
        + ")")
    void saveUserRole(
        @Bind("userId") String userId,
        @Bind("role") String role
    );

    @SqlQuery("SELECT * FROM user_roles WHERE user_roles.user_id = :userId")
    List<UserRole> getByUserId(@Bind("userId") String userId);
}
