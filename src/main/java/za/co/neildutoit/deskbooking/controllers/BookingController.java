package za.co.neildutoit.deskbooking.controllers;

import za.co.neildutoit.deskbooking.db.entity.User;
import za.co.neildutoit.deskbooking.dto.BookingDto;
import za.co.neildutoit.deskbooking.dto.CoordinateDto;
import za.co.neildutoit.deskbooking.dto.MessageDto;
import za.co.neildutoit.deskbooking.dto.UserDto;
import za.co.neildutoit.deskbooking.service.DeskBookingService;
import za.co.neildutoit.deskbooking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/booking")
@RequiredArgsConstructor
public class BookingController {

  private final DeskBookingService deskBookingService;
  private final UserService userService;

  //Book seat
  @PostMapping("/{deskId}")
  public MessageDto bookDesk(@PathVariable long deskId, @RequestParam LocalDate date) {
    log.info("bookDesk - deskId: {}, date: {}", deskId, date);
    deskBookingService.bookDeskForCurrentUser(date, deskId);
    return new MessageDto("OK");
  }

  //Cancel Booking - current user
  @DeleteMapping("/{deskId}")
  public MessageDto cancelBooking(@PathVariable long deskId, @RequestParam LocalDate date) {
    log.info("cancelBooking - deskId: {}, date: {}", deskId, date);
    deskBookingService.cancelBooking(date, deskId);
    return new MessageDto("OK");
  }

  @PostMapping("/{deskId}/reserve")
  public MessageDto reserveDesk(@PathVariable long deskId, @RequestParam long userId) {
    log.info("reserveDesk - deskId: {}, userId: {}", deskId, userId);
    userService.checkAdminUser();
    deskBookingService.reserveDeskForUser(deskId, userId);
    return new MessageDto("OK");
  }

  @DeleteMapping("/{deskId}/reserve")
  public MessageDto cancelReservedDesk(@PathVariable long deskId) {
    log.info("cancelReservedDesk - deskId: {}", deskId);
    userService.checkAdminUser();
    deskBookingService.cancelReservedDesk(deskId);
    return new MessageDto("OK");
  }



  //Get all bookings for person
  @RequestMapping("/all")
  public List<BookingDto> getBookingsForUser() {
    User user = userService.getCurrentUser();
    return deskBookingService.getAllByUser(user.getId());
  }

  @RequestMapping("/permanent/all")
  public List<BookingDto> getAllPermanentReservations() {
    return deskBookingService.getAllPermanentBookings();
  }

  @RequestMapping("/permanent/users/unassigned")
  public List<UserDto> getAllUsersWithoutPermanentReservations() {
    return deskBookingService.getAllUsersWithoutPermanentBookings();
  }

  @RequestMapping("/dates")
  public List<LocalDate> getBookedDatesForUser(@RequestParam("days") Integer days) {
    User user = userService.getCurrentUser();
    return deskBookingService.getBookedDatesByUser(user.getId(), days);
  }

  @RequestMapping("/heatmap")
  public List<CoordinateDto> getHeatMapData() {
    //TODO: Date range
    return deskBookingService.getHeatMapData();
  }

  //Booking report - All persons for date range
}
