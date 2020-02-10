package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.UserRolesRepository;
import uk.gov.hmcts.cmc.domain.models.UserRole;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserRolesService {
    private final UserRolesRepository userRolesRepository;
    private final UserService userService;

    public UserRolesService(
        UserRolesRepository userRolesRepository,
        UserService userService
    ) {
        this.userRolesRepository = userRolesRepository;
        this.userService = userService;
    }

    public List<String> retrieveUserRoles(String authorisation) {
        User user = userService.getUser(authorisation);
        String userId = user.getUserDetails().getId();

        return userRolesRepository.getByUserId(userId)
            .stream()
            .map(UserRole::getRole)
            .collect(Collectors.toList());
    }

    public void saveRole(String userRolesName, String authorisation) {
        User user = userService.getUser(authorisation);
        String userId = user.getUserDetails().getId();
        userRolesRepository.saveUserRole(userId, userRolesName);
    }
}
