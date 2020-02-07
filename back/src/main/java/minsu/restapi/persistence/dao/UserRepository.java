package minsu.restapi.persistence.dao;

import minsu.restapi.persistence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    public boolean existsByName(String name);

    public boolean existsByEmail(String email);

    public void deleteByEmail(String email);

    @Query("select s from User s where s.email = :email")
    User fe(@Param("email") String email);

    Optional<User> findByEmail(String email);
}
