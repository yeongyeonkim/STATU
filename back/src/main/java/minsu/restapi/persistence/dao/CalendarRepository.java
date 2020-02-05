package minsu.restapi.persistence.dao;

import minsu.restapi.persistence.model.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;


public interface CalendarRepository extends JpaRepository<Calendar, Long> {

    public List<Calendar> findByUserId(Long id);

    public Calendar findByUserIdAndRepresent(Long id, boolean represent);

    public boolean existsByUserId(Long userId);

    @Transactional
    @Modifying
    @Query("update Calendar c set c.represent = false where c.represent = true and c.user.id = :userId")
    public void updateRepresent(@Param("userId") Long userId);
}
