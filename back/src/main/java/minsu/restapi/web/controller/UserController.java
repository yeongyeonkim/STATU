package minsu.restapi.web.controller;

import io.swagger.annotations.ApiOperation;
import jdk.nashorn.internal.objects.annotations.Getter;
import minsu.restapi.persistence.model.*;
import minsu.restapi.persistence.service.FileUploadDownloadService;
import minsu.restapi.persistence.service.JwtService;
import minsu.restapi.persistence.service.UserService;
import minsu.restapi.spring.LoginUser;
import minsu.restapi.web.dto.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
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

    @Autowired
    FileUploadDownloadService fileUploadDownloadService;

  /*  @ExceptionHandler
    public Map<String, String> errorHandler(Exception e){
        Map<String, String> map = new HashMap<>();
        map.put("result", "false");
        return map;
    }*/


  @GetMapping("/user/exp")
  public boolean checkExp(HttpServletRequest req){
      return jwtService.getExpToken(req.getHeader("token"));
  }

    @PostMapping("/user/signup")
    @ApiOperation("가입하기")
    public ResponseEntity<Map<String, Object>> postSignUp(@RequestBody UserDto userDto) throws Exception {

        SimpleDateFormat dateFormat = new SimpleDateFormat ( "yyyy-MM-dd");
        Date date = new Date();
        userDto.setId(null);
        User user = convertToEntity(userDto);
        try {
            user.setUserTypeCode("user");
            user.setStatusCode("not_checked");
            user.setRole(Role.GUEST);
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
            System.out.println(reqUser);
            if (reqUser != null) {
                String token = jwtService.create(reqUser);
                res.setHeader("token", token);
                resultMap.put("token",token);
                return response(resultMap, HttpStatus.ACCEPTED, true);
            } else {
                resultMap.put("message", "아이디 혹은 비밀번호가 틀렸습니다. 다시 시도해주세요");
                return response(resultMap, HttpStatus.ACCEPTED, true);
            }
        } catch (Exception e) {
            return response(e.getMessage(), HttpStatus.CONFLICT, false);
        }
    }


    @GetMapping("/user/social")
    @ApiOperation("소셜로그인 인증 후 리다이렉트되는 부분")
    public ResponseEntity<Map<String, Object>> social(@LoginUser SessionUser user){
        Map<String, Object> map = new HashMap<>();
        try{
            if(user != null){
                map.put("user", user);
                return response(map, HttpStatus.ACCEPTED, true);
            } else{
                map.put("message", "아이디 혹은 비밀번호가 틀렸습니다. 다시 시도해주세요");
                return response(map, HttpStatus.ACCEPTED, false);
            }
        } catch (Exception e){
            return response(e.getMessage(), HttpStatus.CONFLICT, false);
        }
    }

    @GetMapping("/user/auth/exp")
    public boolean checkExpiration(HttpServletRequest req){
        return jwtService.getExpToken(req.getHeader("token"));
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

    /*
    @GetMapping("/user/{id}")
    public UserResponseDto findById(@PathVariable Long id) {
        User user = userService.findById(id);
        UserResponseDto userResponseDto = convertToResponseDto(user);
        return userResponseDto;
    }
    */

    @GetMapping("/user/{name}")
    public UserResponseDto findByName(@PathVariable String name) {
        User user = userService.findByName(name);
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



    @PutMapping("/user/auth/modify")
    public ResponseEntity<Map<String, Object>> modify(@RequestBody UserDto userDto) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        User user = convertToEntity(userDto);
        userService.modify(user);
        String token = jwtService.create(user);
        resultMap.put("token",token);
        return response(resultMap, HttpStatus.ACCEPTED, true);
    }

    @DeleteMapping("/user/auth/{email}")
    public Map<String, String> deleteUser(@PathVariable("email") String email) {
        Map<String, String> map = new HashMap<>();
        userService.deleteByEmail(email);
        map.put("result", "success");
        return map;
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
        if(userDto.getCategory1()!=null)
        for(int i=0; i<userDto.getCategory1().length; i++){
            temp+=userDto.getCategory1()[i]+",";
        }
        user.setCategory1(temp);

        temp="";
        if(userDto.getCategory2()!=null)
        for(int i=0; i<userDto.getCategory2().length; i++){
            temp+=userDto.getCategory2()[i]+",";
        }
        user.setCategory2(temp);

        return user;
    }

    @PostMapping("/user/auth/upload")
    public User uploadFile(@RequestParam(value = "file", required = false) MultipartFile file,
                           @RequestParam("email") String email) throws Exception {
        User user = userService.findByEmail(email);
        if(file == null){
            return user;
        }

        File root = new File("./uploads");
        if(root.exists() && user.getImg() != ""){ //파일존재여부
            String temp = user.getImg();
            String[] info = temp.split("/downloadFile/");
            File[] files = root.listFiles();
            for(File f : files){
                if(f.getName().equals(info[1])){
                    f.delete();
                }
            }
        }

        String fileName = fileUploadDownloadService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();
        user.setImg(fileDownloadUri);
        userService.save(user);
        return user;
    }
    @DeleteMapping("/user/auth/deletefile")
    public void deleteFile(@RequestParam("email") String email) throws Exception {
        userService.deleteImg(email);
    }


    private ResponseEntity<Map<String, Object>> response(Object data, HttpStatus httpstatus, boolean status) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", status);
        resultMap.put("data", data);
        System.out.println("data : " + data + ", status  : " + status + ", : httpstatus: " + httpstatus);
        return new ResponseEntity<Map<String, Object>>(resultMap, httpstatus);
    }
}
