package za.co.neildutoit.deskbooking.service;

import za.co.neildutoit.deskbooking.db.Repository.UserRepository;
import za.co.neildutoit.deskbooking.db.entity.User;
import za.co.neildutoit.deskbooking.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public void createLocalUser(OidcUser oidcUser) {
        String oidcId = (String) oidcUser.getAttributes().get("sub");
        Optional<User> userOpt = userRepository.findByOidcId(oidcId);
        log.info("createLocalUser - oidcId: {} - exists: {}", oidcId, userOpt.isPresent());

        User user = userOpt.orElseGet(() -> User.builder().oidcId(oidcId).build());

        String email = (String) oidcUser.getAttributes().get("preferred_username");
        String displayName = (String) oidcUser.getAttributes().get("name");
        log.info("createLocalUser - oidcId: {}, email: {} - not exists", oidcId, email);
        user.setEmail(email);
        user.setDisplayName(displayName);
        userRepository.save(user);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String oidcId = (String) oidcUser.getAttributes().get("sub");
        Optional<User> user = userRepository.findByOidcId(oidcId);
        return user.orElseThrow(UserNotFoundException::new);
    }
}
