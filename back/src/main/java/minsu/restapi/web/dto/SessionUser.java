package minsu.restapi.web.dto;

import lombok.Getter;
import minsu.restapi.persistence.model.User;

import java.io.Serializable;

@Getter
public class SessionUser implements Serializable {
    private String name;
    private String email;

    public SessionUser(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
    }
}