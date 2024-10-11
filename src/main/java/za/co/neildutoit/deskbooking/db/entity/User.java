package za.co.neildutoit.deskbooking.db.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @Builder.Default
    private boolean isAdmin = false;
    @Builder.Default
    private boolean enabled = true;
    private LocalDateTime lastSeen;

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>();

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", oidcId='" + oidcId + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", isAdmin=" + isAdmin +
                ", enabled=" + enabled +
                ", bookings=" + bookings.size() +
                '}';
    }
}
