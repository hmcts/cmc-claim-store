package uk.gov.hmcts.cmc.claimstore.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.repositories.UserRolesRepository;
import uk.gov.hmcts.cmc.domain.models.UserRole;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
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
        log.info("Fetching user roles for user {} with {}",
            user.getUserDetails().getEmail(), user.getUserDetails().getRoles());
        String userId = user.getUserDetails().getId();

        return userRolesRepository.getByUserId(userId)
            .stream()
            .map(UserRole::getRole)
            .collect(Collectors.toList());
    }

    public void saveRole(String userRolesName, String authorisation) {
        User user = userService.getUser(authorisation);
        log.info("Updating user roles for user {} with {} and new roleName {}",
            user.getUserDetails().getEmail(), user.getUserDetails().getRoles(), userRolesName);
        String userId = user.getUserDetails().getId();
        userRolesRepository.saveUserRole(userId, userRolesName);
    }
}
