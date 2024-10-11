package za.co.neildutoit.deskbooking.service;

import za.co.neildutoit.deskbooking.db.Repository.DeskRepository;
import za.co.neildutoit.deskbooking.db.entity.Booking;
import za.co.neildutoit.deskbooking.db.entity.Desk;
import za.co.neildutoit.deskbooking.db.entity.User;
import za.co.neildutoit.deskbooking.dto.DeskDto;
import za.co.neildutoit.deskbooking.enums.DeskStatus;
import za.co.neildutoit.deskbooking.exception.DeskNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeskService {

    private final DeskRepository deskRepository;
    private final BookingService bookingService;
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

        List<Booking> bookings = bookingService.getBookingsForDate(date);
        bookings.addAll(bookingService.getPermanentBookings());
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

    public Desk get(long deskId) {
        Optional<Desk> desk = deskRepository.findById(deskId);
        return desk.orElseThrow(DeskNotFoundException::new);
    }
}
