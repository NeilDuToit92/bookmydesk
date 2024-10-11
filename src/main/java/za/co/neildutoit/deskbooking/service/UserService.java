package za.co.neildutoit.deskbooking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.neildutoit.deskbooking.db.Repository.UserRepository;
import za.co.neildutoit.deskbooking.db.entity.Booking;
import za.co.neildutoit.deskbooking.db.entity.User;
import za.co.neildutoit.deskbooking.dto.BookingDto;
import za.co.neildutoit.deskbooking.dto.UserDto;
import za.co.neildutoit.deskbooking.exception.NotAdminUserException;
import za.co.neildutoit.deskbooking.exception.UserNotFoundException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

  @Transactional(readOnly = true)
  public List<UserDto> getAllUsers() {
    return userRepository.findAll().stream()
            .sorted(Comparator.comparing(User::getDisplayName))
            .map(user -> UserDto.builder()
                    .id(user.getId())
                    .email(user.getEmail().toLowerCase())
                    .displayName(user.getDisplayName())
                    .isAdmin(user.isAdmin())
                    .bookingCount(user.getBookings().size())
                    .bookings(user.getBookings().stream()
                            .sorted(Comparator.comparing(Booking::getDate))
                            .map(booking -> BookingDto.builder()
                                    .databaseId(booking.getId())
                                    .displayId(booking.getDesk().getDisplayName())
                                    .date(booking.getDate())
                                    .permanent(booking.isPermanent())
                                    .build())
                            .collect(Collectors.toList()))
                    .build())
            .collect(Collectors.toList());
  }

  public void checkAdminUser() {
    User user = getCurrentUser();
    if (user == null || !user.isAdmin()) {
      throw new NotAdminUserException("Forbidden: You do not have permission to access this resource.");
    }
  }
}
