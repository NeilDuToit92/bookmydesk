package za.co.neildutoit.deskbooking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import za.co.neildutoit.deskbooking.db.Repository.BookingRepository;
import za.co.neildutoit.deskbooking.db.entity.Booking;
import za.co.neildutoit.deskbooking.db.entity.Desk;
import za.co.neildutoit.deskbooking.db.entity.User;
import za.co.neildutoit.deskbooking.dto.BookingDto;
import za.co.neildutoit.deskbooking.exception.BookingException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

  private final BookingRepository bookingRepository;

  //Get all bookings for date
  public List<Booking> getBookingsForDate(LocalDate date) {
    return bookingRepository.findAllByDate(date);
  }

  public void bookDesk(LocalDate date, User user, Desk desk) {
    log.info("bookDesk - date: {}, user: {}, desk: {}", date, user, desk);

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

    bookingRepository.save(Booking.builder()
            .date(date)
            .user(user)
            .desk(desk)
            .build()
    );
  }

  public void cancelBooking(LocalDate date, User user, Desk desk) {
    log.info("cancelBooking - date: {}, user: {}, desk: {}", date, user, desk);

    //A user can only have one booking per day
    Optional<Booking> existingBookingForUser = bookingRepository.findAllByUserIdAndDeskIdAndDate(user.getId(), desk.getId(), date);
    existingBookingForUser.ifPresent(bookingRepository::delete);
  }

  public List<BookingDto> getAllByUser(long userId) {
    log.info("getAllByUser - userId: {}", userId);
    List<BookingDto> bookingDtos = new ArrayList<>();

    //A desk can not be booked twice for the same day
    List<Booking> bookings = bookingRepository.findAllByUserId(userId);

    for (Booking booking : bookings) {
      //TODO; Build list of DTOs
    }

    return bookingDtos;
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
}
