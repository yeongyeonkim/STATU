package minsu.restapi.web.controller;

import minsu.restapi.persistence.model.*;
import minsu.restapi.persistence.service.SubTitleService;
import minsu.restapi.web.dto.CalendarResponseDto;
import org.modelmapper.ModelMapper;
import minsu.restapi.persistence.service.CalendarService;
import minsu.restapi.persistence.service.CategoryService;
import minsu.restapi.persistence.service.UserService;
import minsu.restapi.web.dto.CalendarDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"*"}, maxAge = 6000)
@RestController
public class CalendarController {

    @Autowired
    CalendarService calendarService;

    @Autowired
    private SubTitleService subTitleService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping("/calendar")
    public Map<String, String> save(@RequestBody CalendarDto calendarDto) throws Exception {
        System.out.println(calendarDto.isRepresent());

        calendarDto.setId(null);
        Calendar calendar = convertToEntity(calendarDto);
        calendar.setRecommend(0);
        calendar.setView(0);
        calendar.setProgress(0);

        Long id = calendarService.save(calendar);
        SubTitle subTitle = new SubTitle(null,"기타","0000-00-00","0000-00-00","gray",calendar,null);
        subTitleService.save(subTitle);

        Map<String, String> map = new HashMap<>();
        map.put("result", "success");
        map.put("id", id.toString());
        return map;
    }

    @PutMapping("/calendar")
    public Map<String, String> modify(@RequestBody CalendarDto calendarDto) throws Exception {

        Calendar calendar = convertToEntity(calendarDto);
        calendarService.save(calendar);
        Map<String, String> map = new HashMap<>();

        map.put("result", "success");
        return map;
    }

    @PutMapping("/pbtoggle/{calendarid}")
    public Map<String, String> pbToggle(@PathVariable Long calendarid) throws Exception {
        calendarService.pbToggle(calendarid);
        Map<String, String> map = new HashMap<>();
        map.put("result", "success");
        return map;
    }


    @PutMapping("/representset/{calendarid}")
    public Map<String, String> setRepresent(@PathVariable Long calendarid) throws Exception {
        calendarService.setRepresent(calendarid);
        Map<String, String> map = new HashMap<>();
        map.put("result", "success");
        return map;
    }


    @GetMapping("/calendar")
    public List<CalendarResponseDto> findAll(){
        List<Calendar> temp = calendarService.findAll();
        List<CalendarResponseDto> list = new ArrayList<>();
        for(int i=0; i<temp.size(); i++){
            list.add(convertToResponseDto(temp.get(i)));
        }
        return list;
    }


    @GetMapping("/calendar/findall/{userid}")
    public List<CalendarResponseDto> findByUserId(@PathVariable Long userid){
        List<Calendar> temp = calendarService.findByUserId(userid);
        List<CalendarResponseDto> list = new ArrayList<>();
        for(int i=0; i<temp.size(); i++){
            list.add(convertToResponseDto(temp.get(i)));
        }
        return list;
    }

    @GetMapping("/calendar/represent/{userid}")
    public CalendarResponseDto findByUserIdAndRepresent(@PathVariable Long userid){
        return convertToResponseDto(calendarService.findByUserIdAndRepresent(userid));
    }

    @DeleteMapping("/calendar/{calendarId}")
    public Map<String, String> delete(@PathVariable Long calendarId){
        calendarService.deleteById(calendarId);
        Map<String, String> map = new HashMap<>();
        map.put("result", "success");
        return map;
    }


    //mapper

    private CalendarResponseDto convertToResponseDto(Calendar calendar){
        CalendarResponseDto calendarResponseDto = modelMapper.map(calendar, CalendarResponseDto.class);
        //set
        String temp[] = calendar.getTag().split(",");
        calendarResponseDto.setTags(temp);

        temp = calendar.getCategory1().split(",");
        calendarResponseDto.setCategory1(temp);

        temp = calendar.getCategory2().split(",");
        calendarResponseDto.setCategory2(temp);
        return calendarResponseDto;
    }

    private Calendar convertToEntity(CalendarDto calendarDto) throws Exception{

        Calendar calendar = modelMapper.map(calendarDto, Calendar.class);

        //set
        String temp="";
        for(int i=0; i<calendarDto.getTags().length; i++){temp+=calendarDto.getTags()[i]+",";}
        calendar.setTag(temp);
       temp="";
        for(int i=0; i<calendarDto.getCategory1().length; i++){temp+=calendarDto.getCategory1()[i]+",";}
        calendar.setCategory1(temp);
        temp="";
        for(int i=0; i<calendarDto.getCategory2().length; i++){temp+=calendarDto.getCategory2()[i]+",";}
        calendar.setCategory2(temp);
        return calendar;
    }


}

