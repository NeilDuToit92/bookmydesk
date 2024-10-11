package za.co.neildutoit.deskbooking.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserDto {
  private long id;
  private String email;
  private String displayName;
  private boolean isAdmin;
  private List<BookingDto> allBookings;
  private List<BookingDto> upcomingBookings;
  private Integer bookingCount;
  private Integer upcomingBookingCount;
}
