package za.co.neildutoit.deskbooking.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.neildutoit.deskbooking.db.Repository.BookingRepository;
import za.co.neildutoit.deskbooking.db.Repository.DeskRepository;
import za.co.neildutoit.deskbooking.db.entity.Booking;
import za.co.neildutoit.deskbooking.db.entity.Desk;
import za.co.neildutoit.deskbooking.db.entity.User;
import za.co.neildutoit.deskbooking.dto.BookingDto;
import za.co.neildutoit.deskbooking.dto.CoordinateDto;
import za.co.neildutoit.deskbooking.dto.DeskDto;
import za.co.neildutoit.deskbooking.enums.DeskStatus;
import za.co.neildutoit.deskbooking.exception.BookingException;
import za.co.neildutoit.deskbooking.exception.DeskNotFoundException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeskBookingService {

  private final BookingRepository bookingRepository;
  private final DeskRepository deskRepository;
  private final UserService userService;

  @PostConstruct
  public void start() {
    int id = 1;
    ArrayList<Desk> desks = new ArrayList<>();

    //Right side office
    int[] cols1 = new int[]{1575, 1620, 1665, 1747, 1792, 1837};
    int[] rows1 = new int[]{339, 365, 450, 477, 615, 641, 726, 753};

    for (int row : rows1) {
      for (int col : cols1) {
        desks.add(Desk.builder().id(id).displayName(id++).x(col).y(row).build());
      }
    }

    //Left side office
    int[] cols2 = new int[]{156, 183, 252, 278, 412, 439, 523, 549};
    int[] rows2 = new int[]{732, 778, 928, 973, 1019};

    for (int row : rows2) {
      for (int col : cols2) {
        desks.add(Desk.builder().id(id).displayName(id++).x(col).y(row).build());
      }
    }

    deskRepository.saveAll(desks);
  }

  /**
   * Retrieves all the desks with their coordinates and status
   *
   * @return List of {@link DeskDto}
   */
  public ArrayList<DeskDto> getDesksForLayout(LocalDate date) {
    ArrayList<DeskDto> desks = new ArrayList<>();

    //Do not show desks when selected date falls on a weekend
    if (DayOfWeek.SATURDAY.equals(date.getDayOfWeek()) || DayOfWeek.SUNDAY.equals(date.getDayOfWeek())) {
      return desks;
    }

    List<Booking> bookings = getBookingsForDate(date);
    bookings.addAll(getPermanentBookings());
    User currentUser = userService.getCurrentUser();

    for (Desk desk : deskRepository.findAll()) {

      Booking bookingForDesk = null;
      boolean bookingForCurrentUser = false;
      for (Booking booking : bookings) {
        if (booking.getDesk().equals(desk)) {
          bookingForDesk = booking;
          if (bookingForDesk.getUser().getId() == currentUser.getId()) {
            bookingForCurrentUser = true;
          }
        }
      }

      DeskDto dto = DeskDto.builder()
              .databaseId(desk.getId())
              .displayId(desk.getDisplayName())
              .x(desk.getX())
              .y(desk.getY())
              .status(DeskStatus.AVAILABLE)
              .build();

      if (bookingForDesk != null) {
        if (bookingForDesk.isPermanent()) {
          dto.setStatus(DeskStatus.RESERVED);
        } else {
          dto.setStatus(DeskStatus.BOOKED);
        }
        dto.setBookedByCurrentUser(bookingForCurrentUser);
        dto.setBookedBy(bookingForDesk.getUser().getDisplayName());
      }

      desks.add(dto);
    }
    return desks;
  }

  public Desk getDesk(long deskId) {
    Optional<Desk> desk = deskRepository.findById(deskId);
    return desk.orElseThrow(DeskNotFoundException::new);
  }

  public List<DeskDto> getAllDesks() {
    ArrayList<DeskDto> desks = new ArrayList<>();
    List<Booking> reservedDesks = getPermanentBookings();

    for (Desk desk : deskRepository.findAll()) {

      Booking bookingForDesk = null;
      for (Booking booking : reservedDesks) {
        if (booking.getDesk().equals(desk)) {
          bookingForDesk = booking;
        }
      }

      DeskDto dto = DeskDto.builder()
              .databaseId(desk.getId())
              .displayId(desk.getDisplayName())
              .x(desk.getX())
              .y(desk.getY())
              .status(DeskStatus.AVAILABLE)
              .build();

      if (bookingForDesk != null) {
        dto.setStatus(DeskStatus.RESERVED);
        dto.setBookedBy(bookingForDesk.getUser().getDisplayName());
      }

      desks.add(dto);
    }

    desks.sort(Comparator.comparingLong(DeskDto::getDatabaseId));
    return desks;
  }

  //Get all bookings for date
  public List<Booking> getBookingsForDate(LocalDate date) {
    return bookingRepository.findAllByDate(date);
  }

  public List<Booking> getPermanentBookings() {
    return bookingRepository.findAllByPermanentTrue();
  }

  @Transactional
  public void bookDeskForCurrentUser(LocalDate date, long deskId) {
    log.info("bookDesk - date: {}, deskId: {}", date, deskId);
    User user = userService.getCurrentUser();
    Desk desk = getDesk(deskId);

    //Do not allow weekend booking
    if (DayOfWeek.SATURDAY.equals(date.getDayOfWeek()) || DayOfWeek.SUNDAY.equals(date.getDayOfWeek())) {
      throw new BookingException("Weekend booking is not allowed");
    }

    //Do not historic date booking
    if (date.isBefore(LocalDate.now())) {
      throw new BookingException("Booking for historic dates is not allowed");
    }

    //Only allow booking for X weeks in advance
    //TODO: Make this configurable
    int weeksAllowed = 4;
    if (date.isAfter(LocalDate.now().plusWeeks(weeksAllowed))) {
      throw new BookingException("Booking more than " + weeksAllowed + " weeks in advance is not allowed");
    }

    //A permanently reserved desk can not be booked
    Optional<Booking> permanentBookingForDesk = bookingRepository.findAllByDeskIdAndAndPermanentTrue(desk.getId());
    if (permanentBookingForDesk.isPresent()) {
      throw new BookingException("The desk is reserved");
    }

    //A desk can not be booked twice for the same day
    Optional<Booking> existingBookingForDesk = bookingRepository.findAllByDeskIdAndDate(desk.getId(), date);
    if (existingBookingForDesk.isPresent()) {
      throw new BookingException("The desk has already been booked");
    }

    //A user can only have one booking per day
    Optional<Booking> existingBookingForUser = bookingRepository.findAllByUserIdAndDate(user.getId(), date);
    if (existingBookingForUser.isPresent()) {
      throw new BookingException("Only one desk is allowed per day");
    }

    bookingRepository.save(Booking.builder()
            .date(date)
            .user(user)
            .desk(desk)
            .build()
    );
  }

  @Transactional
  public void cancelBooking(LocalDate date, long deskId) {
    log.info("cancelBooking - date: {}, deskId: {}", date, deskId);
    User user = userService.getCurrentUser();
    Desk desk = getDesk(deskId);

    //A permanently reserved desk can not be unbooked by normal user
    Optional<Booking> permanentBookingForDesk = bookingRepository.findAllByDeskIdAndAndPermanentTrue(desk.getId());
    if (permanentBookingForDesk.isPresent()) {
      throw new BookingException("The desk is reserved");
    }

    Optional<Booking> existingBookingForUser = bookingRepository.findAllByDeskIdAndDate(desk.getId(), date);
    if (existingBookingForUser.isPresent()) {
      Booking booking = existingBookingForUser.get();
      if (booking.getUser().getId() != user.getId()) {
        throw new BookingException("Unable to cancel another user's booking");
      }
      bookingRepository.delete(existingBookingForUser.get());
    } else {
      throw new BookingException("No booking found to cancel");
    }
  }

  @Transactional(readOnly = true)
  public List<BookingDto> getAllByUser(long userId) {
    log.info("getAllByUser - userId: {}", userId);
    return bookingRepository.findAllByUserId(userId).stream()
            .sorted(Comparator.comparing(Booking::getDate))
            .map(booking -> BookingDto.builder()
                    .databaseId(booking.getId())
                    .displayId(booking.getDesk().getDisplayName())
                    .date(booking.getDate())
                    .permanent(booking.isPermanent())
                    .bookedBy(booking.getUser().getDisplayName())
                    .userId(booking.getUser().getId())
                    .build())
            .collect(Collectors.toList());
  }

  public List<LocalDate> getBookedDatesByUser(long userId, int days) {
    log.info("getBookedDatesByUser - userId: {}, days: {}", userId, days);
    List<LocalDate> bookedDates = new ArrayList<>();

    //A desk can not be booked twice for the same day
    List<Booking> bookings = bookingRepository.findAllByUserIdAndDateUpTo(userId, LocalDate.now().plusDays(days));

    for (Booking booking : bookings) {
      bookedDates.add(booking.getDate());
    }

    return bookedDates;
  }

  @Transactional
  public void reserveDeskForUser(long deskId, long userId) {
    //TODO: There should be a step that show in the FE which future bookings will be cancelled
    log.info("reserveDeskForUser - deskId: {}, userId: {}", deskId, userId);
    Desk desk = getDesk(deskId);

    //Check if desk is already permanently booked
    Optional<Booking> permanentBookingForDesk = bookingRepository.findAllByDeskIdAndAndPermanentTrue(desk.getId());
    if (permanentBookingForDesk.isPresent()) {
      throw new BookingException("The desk is reserved");
    }

    User user = userService.getUser(userId);
    if (user == null) {
      throw new BookingException("No user found for ID " + userId);
    }

    List<Booking> existingBookingsForDesk = bookingRepository.findAllByDeskIdAndDateOnOrAfter(desk.getId(), LocalDate.now());
//    bookingRepository.deleteAll(existingBookingsForDesk);

//    bookingRepository.save(Booking.builder()
//            .date(LocalDate.now())
//            .user(user)
//            .desk(desk)
//              .permanent(true)
//            .build()
//    );
  }

  @Transactional(readOnly = true)
  public List<BookingDto> getAllPermanentBookings() {
    return bookingRepository.findAllByPermanentTrue().stream()
            .sorted(Comparator.comparing(booking -> booking.getUser().getDisplayName()))
            .map(booking -> BookingDto.builder()
                    .databaseId(booking.getId())
                    .displayId(booking.getDesk().getDisplayName())
                    .date(booking.getDate())
                    .permanent(booking.isPermanent())
                    .bookedBy(booking.getUser().getDisplayName())
                    .userId(booking.getUser().getId())
                    .build())
            .collect(Collectors.toList());
  }

  @Transactional
  public List<CoordinateDto> getHeatMapData() {
    return bookingRepository.findAll().stream()
            .map(booking -> CoordinateDto.builder()
                    .x(booking.getDesk().getX())
                    .y(booking.getDesk().getY())
                    .build())
            .collect(Collectors.toList());
  }
}
