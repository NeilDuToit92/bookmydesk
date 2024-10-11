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
@Table(name = "desk")
public class Desk {

    @Id
    @GeneratedValue
    private long id;
    private long displayName;
    private long x;
    private long y;
}
