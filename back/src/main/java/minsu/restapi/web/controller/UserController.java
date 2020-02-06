package minsu.restapi.web.controller;

import io.swagger.annotations.ApiOperation;
import minsu.restapi.persistence.model.*;
import minsu.restapi.persistence.service.JwtService;
import minsu.restapi.persistence.service.UserService;
import minsu.restapi.web.dto.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

@CrossOrigin(origins = {"*"}, maxAge = 6000)
@RestController
public class UserController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    UserService userService;

    @Autowired
    private ModelMapper modelMapper;

  /*  @ExceptionHandler
    public Map<String, String> errorHandler(Exception e){
        Map<String, String> map = new HashMap<>();
        map.put("result", "false");
        return map;
    }*/


    @PostMapping("/user/signup")
    @ApiOperation("가입하기")
    public ResponseEntity<Map<String, Object>> postSignUp(@RequestBody UserDto userDto) throws Exception {

        SimpleDateFormat dateFormat = new SimpleDateFormat ( "yyyy-MM-dd");
        Date date = new Date();
        userDto.setId(null);
        User user = convertToEntity(userDto);
        try {
            System.out.println(user);
            user.setUserTypeCode("user");
            user.setStatusCode("not_checked");
            if(user.getImg()==null){
                user.setImg("default.png");
            }
            user.setRegDate(dateFormat.format(date));
            int i = userService.save(user);

            if (i == 1) {
                userService.sendEmail(user);

                return response(user, HttpStatus.CREATED, true);
            } else {
                return response("유효하지 않은 접근입니다.", HttpStatus.CONFLICT, false);
            }
        } catch (Exception e) {
            return response(e.getMessage(), HttpStatus.CONFLICT, false);
        }
    }

    //로그인
    @PostMapping("/user/signin")
    @ApiOperation("로그인하기")
    public ResponseEntity<Map<String, Object>> postSignIn(@RequestBody LoginDto loginDto, HttpServletResponse res) {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = null;
        try {
            User reqUser = userService.signin(loginDto.getEmail(), loginDto.getPassword());
            if (reqUser != null) {
                String token = jwtService.create(reqUser);
                System.out.println("token : " + token); // 이게 토큰
                res.setHeader("jwt-auth-token", token);
                resultMap.put("jwt",token);
                resultMap.put("status", true);
                resultMap.put("data", reqUser);
                return response(resultMap, HttpStatus.ACCEPTED, true);
            } else {
                resultMap.put("message", "아이디 혹은 비밀번호가 틀렸습니다. 다시 시도해주세요");
                return response(resultMap, HttpStatus.ACCEPTED, true);
            }
        } catch (Exception e) {
            return response(e.getMessage(), HttpStatus.CONFLICT, false);
        }
    }




    @GetMapping("/user")
    public List<UserResponseDto> findAll() {

        List<User> userList = userService.findAll();
        List<UserResponseDto> list = new ArrayList<>();
        for(int i=0; i<userList.size();i++){
            list.add(i,convertToResponseDto(userList.get(i)));
            System.out.println(list.get(i));
        }

        return list;
    }

    @GetMapping("/user/{id}")
    public UserResponseDto findById(@PathVariable Long id) {
        User user = userService.findById(id);
        UserResponseDto userResponseDto = convertToResponseDto(user);
        return userResponseDto;
    }

    @GetMapping("/user/checkmail/{usermail}")
    public Map<String, String> checkmail(@PathVariable String usermail) {

        Map<String, String> map = new HashMap<>();
        if (userService.checkEmail(usermail)) {
            map.put("result", "true");
        } else {
            map.put("result", "false");
        }
        return map;
    }

    @GetMapping("/user/checkname/{name}")
    public Map<String, String> checkname(@PathVariable String name) {
        Map<String, String> map = new HashMap<>();
        if (userService.checkName(name)) {
            map.put("result", "true");
        } else {
            map.put("result", "false");
        }
        return map;
    }



    @PutMapping("/user")
    public Map<String, String> modify(@RequestBody UserDto userDto) throws Exception {
        User user = convertToEntity(userDto);
        userService.modify(user);
        Map<String, String> map = new HashMap<>();
        map.put("result", "success");
        return map;

    }


    @DeleteMapping("/user/{email}")
    public Map<String, String> deleteUser(@PathVariable String email) {
        Map<String, String> map = new HashMap<>();
        userService.deleteByEmail(email);
        map.put("result", "success");
        return map;

    }

    private ResponseEntity<Map<String, Object>> response(Object data, HttpStatus httpstatus, boolean status) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", status);
        resultMap.put("data", data);
        System.out.println("data : " + data + ", status  : " + status + ", : httpstatus: " + httpstatus);
        return new ResponseEntity<Map<String, Object>>(resultMap, httpstatus);
    }

    @GetMapping(value="/joinConfirm/{id}/{auth}")
    public String  emailConfirm(@PathVariable Long id,@PathVariable String auth,HttpServletResponse response) throws Exception {
        User user = userService.findById(id);
        if (user.getAuthKey().compareTo(auth)==0) {
            user.setStatusCode("use");    // authstatus를 1로,, 권한 업데이트
            userService.save(user);
            return "<h1>인증 성공</h1>";
        } else {
            return "<h1>인증 실패</h1>";
        }

    }


    //mapper

    private UserResponseDto convertToResponseDto(User user){
        UserResponseDto userResponseDto = modelMapper.map(user, UserResponseDto.class);

        String[] temp = user.getCategory1().split(",");
        userResponseDto.setCategory1(temp);

        temp = user.getCategory2().split(",");
        userResponseDto.setCategory2(temp);
        return userResponseDto;
    }



    private User convertToEntity(UserDto userDto) throws Exception {

        User user = modelMapper.map(userDto, User.class);

        String temp="";
        for(int i=0; i<userDto.getCategory1().length; i++){
            temp+=userDto.getCategory1()[i]+",";
        }
        user.setCategory1(temp);

        temp="";
        for(int i=0; i<userDto.getCategory2().length; i++){
            temp+=userDto.getCategory2()[i]+",";
        }
        user.setCategory2(temp);

        return user;
    }
}
