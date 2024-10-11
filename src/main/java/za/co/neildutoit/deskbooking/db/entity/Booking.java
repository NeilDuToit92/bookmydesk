package za.co.neildutoit.deskbooking.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

/**
 * A reservation is for when a desk i
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "booking")
public class Booking {

    @Id
    @GeneratedValue
    private long id;

    /**
     * The desk to book
     */
    @ManyToOne
    @JoinColumn(name = "desk_id")
    private Desk desk;

    /**
     * The user the desk is booked for
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The date for which the desk is reserved - unless permanent is set to true
     */
    @Nullable
    private LocalDate date;

    /**
     * Desk is booked permanently and will display blue in the UI
     */
    @Builder.Default
    private boolean permanent = false;
}
