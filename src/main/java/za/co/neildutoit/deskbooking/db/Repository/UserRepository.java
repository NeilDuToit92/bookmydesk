package za.co.neildutoit.deskbooking.db.Repository;

import za.co.neildutoit.deskbooking.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOidcId(String oidcId);
}