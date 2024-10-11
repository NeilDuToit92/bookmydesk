package za.co.neildutoit.deskbooking.controllers;

import za.co.neildutoit.deskbooking.db.entity.Desk;
import za.co.neildutoit.deskbooking.db.entity.User;
import za.co.neildutoit.deskbooking.dto.BookingDto;
import za.co.neildutoit.deskbooking.service.BookingService;
import za.co.neildutoit.deskbooking.service.DeskService;
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

    private final BookingService bookingService;
    private final UserService userService;
    private final DeskService deskService;

    //Book seat
    @PostMapping("/{deskId}")
    public String bookDesk(@PathVariable long deskId, @RequestParam LocalDate date) {
        log.info("bookDesk - deskId: {}, date: {}", deskId, date);
        User user = userService.getCurrentUser();
        Desk desk = deskService.get(deskId);
        bookingService.bookDesk(date, user, desk);
        return "OK";
    }

    //Cancel Booking - current user
    @DeleteMapping("/{deskId}")
    public String cancelBooking(@PathVariable long deskId, @RequestParam LocalDate date) {
        log.info("cancelBooking - deskId: {}, date: {}", deskId, date);
        User user = userService.getCurrentUser();
        Desk desk = deskService.get(deskId);
        bookingService.cancelBooking(date, user, desk);
        return "OK";
    }

    //Get all bookings for person
    @RequestMapping("/all")
    public List<BookingDto> getBookingsForUser() {
        User user = userService.getCurrentUser();
        return bookingService.getAllByUser(user.getId());
    }

    //Booking report - All persons for date range
}
