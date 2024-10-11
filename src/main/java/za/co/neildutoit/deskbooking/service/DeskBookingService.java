package za.co.neildutoit.deskbooking.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
import za.co.neildutoit.deskbooking.dto.UserDto;
import za.co.neildutoit.deskbooking.enums.DeskStatus;
import za.co.neildutoit.deskbooking.exception.BookingException;
import za.co.neildutoit.deskbooking.exception.DeskNotFoundException;

import java.awt.print.Book;
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

//    @PostConstruct
    public void start() {
        int id = 1;
        ArrayList<Desk> desks = new ArrayList<>();

        //Right side office
        int[] cols1 = new int[]{1583, 1629, 1675, 1755, 1802, 1848};
        int[] rows1 = new int[]{317, 343, 428, 455, 593, 620, 705, 731};
        for (int row : rows1) {
            for (int col : cols1) {
                desks.add(Desk.builder().id(id).displayName(id++).x(col).y(row).build());
            }
        }

        //Left side office
        int[] cols2 = new int[]{164, 191, 260, 286, 420, 447, 531, 558};
        int[] rows2 = new int[]{712, 758, 908, 953, 999};
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

//    @Cacheable(value = "bookingsForDate", key = "#date")
    public ArrayList<DeskDto> getDesksForLayout(LocalDate date) {
        log.info("getDesksForLayout - date: {}", date);
        ArrayList<DeskDto> desks = new ArrayList<>();

        //Do not show desks when selected date falls on a weekend
        if (DayOfWeek.SATURDAY.equals(date.getDayOfWeek()) || DayOfWeek.SUNDAY.equals(date.getDayOfWeek())) {
            return desks;
        }

        List<Booking> bookings = getBookingsForDate(date);
        //Add permanent bookings
        for (Booking booking : getPermanentBookings()) {
            Booking existingBooking = null;

            //Permanent bookings supersede the date based bookings
            for (Booking existingBookingIt : bookings) {
                if (existingBookingIt.getDesk().getId() == booking.getDesk().getId()) {
                    existingBooking = existingBookingIt;
                }
            }
            if (existingBooking != null) {
                bookings.remove(existingBooking);
            }
            bookings.add(booking);
        }

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

//    @Cacheable(value = "allDesks")
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
    @CacheEvict(value = "bookingsForDate", key = "#date")
    public void bookDeskForCurrentUser(LocalDate date, long deskId) {
        log.info("bookDesk - date: {}, deskId: {}", date, deskId);
        User user = userService.getCurrentUser();
        Desk desk = getDesk(deskId);

        if (date == null) {
            throw new BookingException("The date can not be null");
        }

        bookDesk(date, desk, user, false);
    }

    /**
     *
     * @param deskId
     * @param userId
     * @return bookings cancelled caused by reserving this desk
     */
    @Transactional
    @CacheEvict(value = {"bookingsForDate", "permanentBookings", "allDesks"}, allEntries = true)
    public List<BookingDto> reserveDeskForUser(long deskId, long userId) {
        log.info("reserveDeskForUser - deskId: {}, userId : {}", deskId, userId);
        User user = userService.getUser(userId);
        Desk desk = getDesk(deskId);

        List<Booking> permanentBookingForUser = bookingRepository.findAllByUserIdAndPermanentTrue(desk.getId());
        if (!permanentBookingForUser.isEmpty()) {
            throw new BookingException("The user already has a reserved desk");
        }

        //Get a list of bookings that will be cancelled
        List<Booking> cancelledBookings = bookingRepository.findAllByDeskIdAndDateOnOrAfter(deskId, LocalDate.now());
        cancelledBookings.addAll(bookingRepository.findAllByUserIdAndDateOnOrAfter(userId, LocalDate.now()));
        log.info("reserveDeskForUser - cancelling bookings: {}", cancelledBookings);

        //Cancel any date based bookings made for the desk
        bookingRepository.deleteAll(cancelledBookings);
        //Cancel any bookings made for user on other desks
        bookingRepository.deleteByUserIdAndDateOnOrAfter(userId, LocalDate.now());

        bookDesk(null, desk, user, true);

        return cancelledBookings.stream()
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
    @CacheEvict(value = {"bookingsForDate", "permanentBookings", "allDesks"}, allEntries = true)
    public void cancelReservedDesk(long deskId) {
        log.info("cancelReservedDesk - deskId: {}", deskId);
        List<Booking> permanentBookingForUser = bookingRepository.findAllByDeskIdAndPermanentTrue(deskId);
        if (permanentBookingForUser.isEmpty()) {
            throw new BookingException("Unable to locate the reserved desk");
        } else {
            bookingRepository.deleteAll(permanentBookingForUser);
        }
    }

    private void bookDesk(LocalDate date, Desk desk, User user, boolean permanent) {
        log.info("bookDesk - date: {}, desk: {}, user: {}", date, desk, user);
        if (desk == null) {
            throw new BookingException("Unable to find the selected desk");
        }

        if (user == null) {
            throw new BookingException("Unable to find the selected user");
        }

        //A permanently reserved desk can not be booked
        List<Booking> permanentBookingForDesk = bookingRepository.findAllByDeskIdAndPermanentTrue(desk.getId());
        if (!permanentBookingForDesk.isEmpty()) {
            throw new BookingException("The desk is reserved");
        }

        if (date != null) {
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
        }

        bookingRepository.save(Booking.builder()
                .date(date)
                .user(user)
                .desk(desk)
                .permanent(permanent)
                .build()
        );
    }

    @Transactional
    @CacheEvict(value = "bookingsForDate", key = "#date")
    public void cancelBooking(LocalDate date, long deskId) {
        log.info("cancelBooking - date: {}, deskId: {}", date, deskId);
        User user = userService.getCurrentUser();
        Desk desk = getDesk(deskId);

        //A permanently reserved desk can not be unbooked by normal user
        List<Booking> permanentBookingForDesk = bookingRepository.findAllByDeskIdAndPermanentTrue(desk.getId());
        if (!permanentBookingForDesk.isEmpty()) {
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

        List<Booking> permanentBookings = bookingRepository.findAllByUserIdAndPermanentTrue(userId);
        if (!permanentBookings.isEmpty()) {
            LocalDate start = LocalDate.now();
            LocalDate end = LocalDate.now().plusDays(days);
            while (start.isBefore(end)) {
                bookedDates.add(start);
                start = start.plusDays(1);
            }
        }

        return bookedDates;
    }

    @Transactional(readOnly = true)
//    @Cacheable(value = "permanentBookings")
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

    public List<UserDto> getAllUsersWithoutPermanentBookings() {
        Set<Long> bookedUserIds = bookingRepository.findAllByPermanentTrue().stream()
                .map(booking -> booking.getUser().getId())
                .collect(Collectors.toSet());
        return userService.getAllUsers().stream()
                .filter(user -> !bookedUserIds.contains(user.getId()))
                .toList();
    }

    @CacheEvict(value = "allDesks", allEntries = true)
    public List<DeskDto> saveDesks() {
        return null;
    }
}
