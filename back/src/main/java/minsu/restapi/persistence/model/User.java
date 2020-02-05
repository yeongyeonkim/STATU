package minsu.restapi.persistence.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name="user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "email")
    private String email;
    @Column(name = "password",nullable = false)
    private String password;
    @Column(name = "name",nullable = false)
    private String name;
    @Column(name = "img",nullable = true,columnDefinition = "varchar(225) default 'default.png'")
    private String img;

    @Column(name ="category1")
    private String category1;
    @Column(name ="category2")
    private String category2;

    @Column(name = "reg_date")
    private String regDate;
    //사용자 타입
    //@Column(columnDefinition="USER")
    @Column(name = "user_type_code", columnDefinition = "varchar(20) default 'user'")
    private String userTypeCode;

    @Column(name="auth_key")
    private String authKey;


    //사용자 상태
    //@Column(columnDefinition="USE")
    @Column(name = "status_code", columnDefinition = "varchar(20) default 'not_checked'")
    private String statusCode;


    @OneToMany(mappedBy="user")
    private List<Calendar> calendars = new ArrayList<>();

}
