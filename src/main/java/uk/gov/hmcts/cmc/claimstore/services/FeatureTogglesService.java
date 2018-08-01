package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.UserAuthorizedRolesRepository;
import uk.gov.hmcts.cmc.domain.models.AuthorizedRole;
import uk.gov.hmcts.cmc.domain.models.UserRole;

@Service
public class FeatureTogglesService {
    private final UserAuthorizedRolesRepository userAuthorizedRolesRepository;
    private final UserService userService;

    public FeatureTogglesService(
        UserAuthorizedRolesRepository userAuthorizedRolesRepository,
        UserService userService
    ) {
        this.userAuthorizedRolesRepository = userAuthorizedRolesRepository;
        this.userService = userService;
    }

    public String authorizedRole(String authorisation) {
        User user = userService.getUser(authorisation);
        String userId = user.getUserDetails().getId().toString();

        return userAuthorizedRolesRepository.getByUserId(userId)
            .orElseThrow(() -> new NotFoundException("Claim not found by id " + userId)
            ).getRole();
    }

    public AuthorizedRole saveRole(UserRole userRole, String authorisation) {
        User user = userService.getUser(authorisation);
        String userId = user.getUserDetails().getId().toString();
        userAuthorizedRolesRepository.saveAuthorizedUserRoles(userId, userRole.getRoleName());

        return userAuthorizedRolesRepository.getByUserId(userId)
            .orElseThrow(() -> new NotFoundException("Claim not found by id " + userId)
            );
    }
}
