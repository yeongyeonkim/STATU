package minsu.restapi.web.controller;

import minsu.restapi.persistence.model.Todo;
import minsu.restapi.persistence.service.SubTitleService;
import minsu.restapi.persistence.service.TodoService;
import minsu.restapi.web.dto.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = {"*"}, maxAge = 6000)
@RestController
public class TodoController {

    @Autowired
    TodoService todoService;

    @Autowired
    SubTitleService subTitleService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/todo")
    public List<TodoResponseDto> findAll(){

        List<Todo> todoList= todoService.findAll();
        List<TodoResponseDto> list = new ArrayList<>();

        for(int i=0; i<todoList.size(); i++){
            list.add(i,convertToResponseDto(todoList.get(i)));
        }

        return list;
    }

    @GetMapping("/todo/{today}/{calenderId}")
    public List<TodoResponseDto> findByDateCal(@PathVariable Date date, @PathVariable Long calenderId){
        List<Todo> todoList= todoService.findByDateCal(date,calenderId);
        List<TodoResponseDto> list = new ArrayList<>();

        for(int i=0; i<todoList.size(); i++){
            list.add(i,convertToResponseDto(todoList.get(i)));
        }
        return list;
    }

    @DeleteMapping("/todo/{todoId}")
    public Map<String, String> deleteById(@PathVariable Long todoId){
        Map<String, String> map = new HashMap<>();
        todoService.deleteById(todoId);
        map.put("result", "success");
        return map;
    }

    @PostMapping("/todo")
    public Map<String, Object> save(@RequestBody TodoDto todoDto) throws Exception {
        todoDto.setId(null);
        Todo todo = convertToEntity(todoDto);
        Long id = todoService.save(todo);

        Map<String, Object> map = new HashMap<>();
        map.put("result", "success");
        map.put("id",id);
        return map;

    }

    @PutMapping("/todo")
    public Map<String, String> updateTodo(@RequestBody TodoDto todoDto) throws Exception {
        Todo todo = convertToEntity(todoDto);
        todoService.save(todo);
        Map<String, String> map = new HashMap<>();
        map.put("result", "success");
        return map;

    }

    //mapper

    private TodoResponseDto convertToResponseDto(Todo todo){
        TodoResponseDto todoResponseDto = modelMapper.map(todo, TodoResponseDto.class);
        return todoResponseDto;
    }


    private Todo convertToEntity(TodoDto todoDto) throws Exception{
        Todo todo = modelMapper.map(todoDto, Todo.class);
        return todo;
    }

}
