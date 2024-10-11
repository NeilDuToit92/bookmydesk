package za.co.neildutoit.deskbooking.db.Repository;

import za.co.neildutoit.deskbooking.db.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByDate(LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.desk.id = ?1 and b.date = ?2")
    Optional<Booking> findAllByDeskIdAndDate(long deskId, LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.user.id = ?1 and b.date = ?2")
    Optional<Booking> findAllByUserIdAndDate(long userId, LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.user.id = ?1")
    List<Booking> findAllByUserId(long userId);

    @Query("SELECT b FROM Booking b WHERE b.user.id = ?1 and b.desk.id = ?2 and b.date = ?3")
    Optional<Booking> findAllByUserIdAndDeskIdAndDate(long userId, long deskId, LocalDate date);
}