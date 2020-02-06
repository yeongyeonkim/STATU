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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    //사용자 상태
    //@Column(columnDefinition="USE")
    @Column(name = "status_code", columnDefinition = "varchar(20) default 'not_checked'")
    private String statusCode;


    @OneToMany(mappedBy="user",fetch = FetchType.LAZY)
    private List<Calendar> calendars = new ArrayList<>();

    @Builder
    public User(String name, String email, Role role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }
    public User update(String name) {
        this.name = name;
        return this;
    }
    public String getRoleKey() {
        return this.role.getKey();
    }
}
