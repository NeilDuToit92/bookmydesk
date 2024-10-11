package za.co.neildutoit.deskbooking.db.Repository;

import za.co.neildutoit.deskbooking.db.entity.Desk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeskRepository extends JpaRepository<Desk, Long> {

}