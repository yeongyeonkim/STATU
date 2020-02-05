package minsu.restapi.web.controller;

import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.annotations.ApiOperation;
import minsu.restapi.persistence.model.*;
import minsu.restapi.persistence.service.FileUploadDownloadService;
import minsu.restapi.persistence.service.JwtService;
import minsu.restapi.persistence.service.UserService;
import minsu.restapi.spring.LoginUser;
import minsu.restapi.web.dto.LoginDto;
import minsu.restapi.web.dto.SessionUser;
import minsu.restapi.web.dto.UserDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/user")
    public List<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/user/{id}")
    public User findById(@PathVariable Long id) {
        return userService.findById(id);
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

    //소셜로그인
//    @GetMapping("/user/social")
//    @ApiOperation("소셜로그인 인증 후 리다이렉트되는 부분")
//    public Map<String, Object> social(@LoginUser SessionUser user){
//        Map<String, Object> map = new HashMap<>();
//        map.put("user", user);
//        return map;
//    }
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

//    @GetMapping("/user/loginfailure")
//    @ApiOperation("로그인 실패시")
//    public String loginfailure(@LoginUser SessionUser user){
//
//    }

    //로그인
    @PostMapping("/user/signin")
    @ApiOperation("로그인하기")
    public ResponseEntity<Map<String, Object>> postSignIn(@RequestBody LoginDto loginDto, HttpServletResponse res) {
        Map<String, Object> resultMap = new HashMap<>();
        HttpStatus status = null;
        try {
            User reqUser = userService.signin(loginDto.getEmail(), loginDto.getPassword());
            if (reqUser != null) {
                String token = jwtService.create(reqUser); // token은 String으로.
                res.setHeader("jwt-auth-token", token);
//                if(jwtService.getExpToken(res.getHeader("jwt_auth-token"))); // true면 만료?
                resultMap.put("token", token);
                return response(resultMap, HttpStatus.ACCEPTED, true);
            } else {
                resultMap.put("message", "아이디 혹은 비밀번호가 틀렸습니다. 다시 시도해주세요");
                return response(resultMap, HttpStatus.ACCEPTED, false);
            }
        } catch (Exception e) {
            return response(e.getMessage(), HttpStatus.CONFLICT, false);
        }
    }

    @PostMapping("/user/signup")
    @ApiOperation("가입하기")
    public ResponseEntity<Map<String, Object>> postSignUp(@RequestBody UserDto userDto) throws Exception {
        userDto.setId(null);
        User user = convertToEntity(userDto);
        try {
            System.out.println(user);
            user.setUserTypeCode("user");
            user.setStatusCode("not_checked");
            user.setRole(Role.GUEST);
            if(user.getImg()==null){
                user.setImg("default.png");
            }
            int i = userService.save(user);
            if (i == 1) {
//                userService.sendEmail(user);
                return response(user, HttpStatus.CREATED, true);
            } else {
                return response("유효하지 않은 접근입니다.", HttpStatus.CONFLICT, false);
            }
        } catch (Exception e) {
            return response(e.getMessage(), HttpStatus.CONFLICT, false);
        }
    }


    @PutMapping("/user/auth")
    public Map<String, String> modify(@RequestBody UserDto userDto, HttpServletRequest req) throws Exception {
//        String jwt = req.getParamter("jwt");
//        String token = req.getHeader("token");
//        System.out.println(token);
//        System.out.println(req.getParameter("jwt-auth-token"));
//        System.out.println(req.getParameter("token"));
//        if(jwtService.checkValid(token)) 면 세이브 하는식으로?
        User user = convertToEntity(userDto);
        userService.save(user);
        Map<String, String> map = new HashMap<>();
        map.put("result", "success");
        return map;
    }


    @DeleteMapping("/user/auth/{email}")
    public Map<String, String> deleteUser(@PathVariable String email, HttpServletRequest req) {
        Map<String, String> map = new HashMap<>();
        userService.deleteByEmail(email);
        map.put("result", "success");
        return map;
    }

    private User convertToEntity(UserDto userDto) throws Exception {

        User user = modelMapper.map(userDto, User.class);

        //set
        if (userDto.getCategory1() != null) {
            for (int i = 0; i < userDto.getCategory1().length; i++) {
                Category1 category1 = new Category1();
                category1.setId(userDto.getCategory1()[i]);
                user.getCategory1s().add(category1);
            }
        }

        if (userDto.getCategory2() != null) {
            for (int i = 0; i < userDto.getCategory2().length; i++) {
                Category2 category2 = new Category2();
                category2.setId(userDto.getCategory2()[i]);
                user.getCategory2s().add(category2);
            }
        }

        return user;
    }

    private ResponseEntity<Map<String, Object>> response(Object data, HttpStatus httpstatus, boolean status) {
        Map<String, Object> resultMap = new HashMap<>();
        System.out.println("status : " + status);
        resultMap.put("status", status);
        resultMap.put("data", data);
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

    @PostMapping("/user/auth/upload")
    public User uploadFile(@RequestParam(value = "file", required = false) MultipartFile file,
                           @RequestParam("email") String email) {
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
//                    System.out.println(f.getName() + " : 파일성공삭제");
                    f.delete();
                }
            }
        }

        String fileName = fileUploadDownloadService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();
        System.out.println(fileName+ " : " + fileDownloadUri);
        user.setImg(fileDownloadUri);
        userService.save(user);
        return user;
    }
    @DeleteMapping("/user/auth/deletefile")
    public void deleteFile(@RequestParam("email") String email){
        userService.deleteImg(email);
    }


}
