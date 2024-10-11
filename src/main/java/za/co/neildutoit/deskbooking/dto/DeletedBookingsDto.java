package za.co.neildutoit.deskbooking.dto;

import lombok.Data;

import java.util.List;

@Data
public class DeletedBookingsDto {
    private List<BookingDto> deletedBookings;

    public DeletedBookingsDto(List<BookingDto> deletedBookings) {
        this.deletedBookings = deletedBookings;
    }
}
