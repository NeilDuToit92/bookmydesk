package za.co.neildutoit.deskbooking.db.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue
    private long id;
    private String oidcId;
    private String email;
    private String displayName;

//    @ManyToOne
//    @JoinColumn(name = "booking_id")
//    private Booking booking;
}
