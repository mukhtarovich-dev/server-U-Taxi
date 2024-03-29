package universalgroup.UTaxi.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import universalgroup.UTaxi.Entity.User;

import java.util.Optional;
import java.util.UUID;

@CrossOrigin
public interface AuthRepository extends JpaRepository<User, UUID> {
    Optional<User> findUserByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);
}
