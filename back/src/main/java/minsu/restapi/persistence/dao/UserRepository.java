package minsu.restapi.persistence.dao;

import minsu.restapi.persistence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    public boolean existsByName(String name);

    public boolean existsByEmail(String email);

    public void deleteByEmail(String email);

    public User findByEmail(String email);
}
