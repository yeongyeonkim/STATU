package minsu.restapi.persistence.service;

import minsu.restapi.persistence.dao.CalendarRepository;
import minsu.restapi.persistence.model.Calendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalendarServiceImpl implements CalendarService {

    @Autowired
    private CalendarRepository calendarRepository;

    @Override
    public Long save(Calendar calendar) {

        if(calendar.isRepresent()==true){
            boolean isCalendar =false;
            isCalendar = calendarRepository.existsByUserId(calendar.getUser().getId());
            if(isCalendar == true){
                calendarRepository.updateRepresent(calendar.getUser().getId());
            }
        }

        Long id = calendarRepository.save(calendar).getId();

        return id;
    }

    public void pbToggle(Long id){
        Calendar calendar = calendarRepository.findById(id).get();
        calendar.setPb(!calendar.isPb());
        calendarRepository.save(calendar);
    }

    public void setRepresent(Long id){
        Calendar calendar = calendarRepository.findById(id).get();
        Long UserId = calendar.getUser().getId();
            boolean isCalendar =false;
            isCalendar = calendarRepository.existsByUserId(UserId);
            if(isCalendar == true){
                calendarRepository.updateRepresent(UserId);
            }
            calendar.setRepresent(true);
            calendarRepository.save(calendar);
    }

    @Override
    public Calendar findById(Long id) {
        return calendarRepository.findById(id).get();
    }

    @Override
    public List<Calendar> findAll() {
        return calendarRepository.findAll();
    }

    @Override
    public List<Calendar> findByUserId(Long id) {
        List<Calendar> list = calendarRepository.findByUserId(id);
        return list;
    }

    @Override
    public Calendar findByUserIdAndRepresent(Long id) {
        Calendar calendar = calendarRepository.findByUserIdAndRepresent(id, true);
        return calendar;
    }

    @Override
    public void deleteById(Long id) {
        calendarRepository.deleteById(id);
    }

}
