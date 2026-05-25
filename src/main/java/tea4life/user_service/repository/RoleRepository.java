
package tea4life.user_service.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tea4life.user_service.model.Role;

import java.util.Optional;

/**
 * Admin 2/3/2026
 *
 **/
@Repository
public interface RoleRepository extends JpaRepository<@NonNull Role, @NonNull Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

}
