package minsu.restapi.spring;

import lombok.RequiredArgsConstructor;
import minsu.restapi.persistence.model.Role;
import minsu.restapi.persistence.service.CustomOAuth2UserService;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.util.Arrays;

@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .headers().frameOptions().disable()
                .and()
                    .authorizeRequests()
                    .antMatchers("/**").permitAll()
                    .antMatchers(HttpMethod.GET, "/swagger-ui.html", "/", "/css/**", "/images/**", "/js/**", "/h2-console/**", "/profile").permitAll()
                    .antMatchers("/user/delete", "/user/upload").hasRole(Role.MANAGER.name())
                //Role.MANAGER.name() 관리자 권한을 가진 아이들만 가능한 경로들을 antMatchers에 추가.
                    .anyRequest().authenticated()
                //anyRequest. 이제 나머지 것들은 authenticated 인증된 사용자들에게만 허용하게 한다.
                .and()
                    .logout()
                        .logoutSuccessUrl("/")
                //로그아웃 성공시 url
                .and()
                    .oauth2Login()
                        .defaultSuccessUrl("/redirect")
//                        .defaultSuccessUrl("/user/social") //이걸로 바꿀 예정 RestApi
                            .failureUrl("/loginfailure") // 실패시
                                .userInfoEndpoint()
                                    .userService(customOAuth2UserService);
    }
}