package com.prathameshAIcourceBuilder.AiCourceBuild.config;

import com.prathameshAIcourceBuilder.AiCourceBuild.entity.User;
import com.prathameshAIcourceBuilder.AiCourceBuild.security.JwtAuthenticationFilter;
import com.prathameshAIcourceBuilder.AiCourceBuild.service.JwtService;
import com.prathameshAIcourceBuilder.AiCourceBuild.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
             
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/login/oauth2/code/**").permitAll()
                .requestMatchers("/api/courses").permitAll()
                .requestMatchers("/api/course/{id}").permitAll()

               
                .requestMatchers("/api/course/generate").authenticated()
                .requestMatchers("/api/course/all").authenticated()

                
                .requestMatchers("/api/quiz/**").authenticated()

                
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler((request, response, authentication) -> {
                    try {
                        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

                        String googleId = oauth2User.getAttribute("sub");
                        String email = oauth2User.getAttribute("email");
                        String name = oauth2User.getAttribute("name");
                        String profilePicture = oauth2User.getAttribute("picture");

                        System.out.println("OAuth success for user: " + email);

                      
                        User user = userService.findOrCreateUser(googleId, email, name, profilePicture);

                      
                        String jwtToken = jwtService.generateToken(user);

                      
                        String redirectUrl = frontendUrl + "?token=" + jwtToken;
                        System.out.println("Redirecting to: " + redirectUrl);

                        response.sendRedirect(redirectUrl);

                    } catch (Exception e) {
                        e.printStackTrace();
                        response.sendRedirect(frontendUrl + "?error=auth_failed");
                    }
                })
                .failureHandler((request, response, exception) -> {
                    System.err.println("OAuth2 login failed: " + exception.getMessage());
                    response.sendRedirect(frontendUrl + "?error=auth_failed");
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
