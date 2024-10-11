package za.co.neildutoit.deskbooking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.neildutoit.deskbooking.db.Repository.UserRepository;
import za.co.neildutoit.deskbooking.db.entity.Booking;
import za.co.neildutoit.deskbooking.db.entity.User;
import za.co.neildutoit.deskbooking.dto.BookingDto;
import za.co.neildutoit.deskbooking.dto.UserDto;
import za.co.neildutoit.deskbooking.dto.UserUpdateRequest;
import za.co.neildutoit.deskbooking.exception.NotAdminUserException;
import za.co.neildutoit.deskbooking.exception.UserNotFoundException;

import java.time.LocalDate;
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

    if (!user.isEnabled()) {
      throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token"), "User account disabled");
    }

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
                    .enabled(user.isEnabled())
                    .email(user.getEmail().toLowerCase())
                    .displayName(user.getDisplayName())
                    .isAdmin(user.isAdmin())
                    .bookingCount(user.getBookings().size())
                    .upcomingBookingCount((int) user.getBookings().stream()
                            .filter(booking -> !booking.getDate().isBefore(LocalDate.now()))
                            .count())
                    .allBookings(user.getBookings().stream()
                            .sorted(Comparator.comparing(Booking::getDate))
                            .map(booking -> BookingDto.builder()
                                    .databaseId(booking.getId())
                                    .displayId(booking.getDesk().getDisplayName())
                                    .date(booking.getDate())
                                    .permanent(booking.isPermanent())
                                    .build())
                            .collect(Collectors.toList()))
                    .upcomingBookings(user.getBookings().stream()
                            .filter(booking -> booking.isPermanent() || (booking.getDate() != null && !booking.getDate().isBefore(LocalDate.now())))
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

  public void updateUser(Long userId, UserUpdateRequest request) {
    User currentUser = getCurrentUser();

    Optional<User> userOpt = userRepository.findById(userId);
    if (userOpt.isPresent()) {
      User user = userOpt.get();
      if (request.getAdmin() != null) {
        updateAdmin(currentUser, user, request.getAdmin());
      }
      if (request.getEnabled() != null) {
        updateEnabled(currentUser, user, request.getEnabled());
      }
      userRepository.save(user);
    } else {
      //TODO: Throw exception
    }
  }

  private void updateAdmin(User currentUser, User user, Boolean admin) {
    if (user.getId() != currentUser.getId()) {
      user.setAdmin(admin);
      userRepository.save(user);
    } else {
      //TODO: Throw exception
    }
  }

  private void updateEnabled(User currentUser, User user, Boolean enabled) {
    if (user.getId() != currentUser.getId()) {
      user.setEnabled(enabled);
      userRepository.save(user);
    } else {
      //TODO: Throw exception
    }
  }

  public User getUser(long userId) {
    return userRepository.findById(userId).orElse(null);
  }
}
