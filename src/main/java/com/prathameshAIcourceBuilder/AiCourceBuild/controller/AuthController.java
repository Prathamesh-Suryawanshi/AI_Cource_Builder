package com.prathameshAIcourceBuilder.AiCourceBuild.controller;

import com.prathameshAIcourceBuilder.AiCourceBuild.dto.AuthResponse;
import com.prathameshAIcourceBuilder.AiCourceBuild.dto.UserDTO;
import com.prathameshAIcourceBuilder.AiCourceBuild.entity.User;
import com.prathameshAIcourceBuilder.AiCourceBuild.service.JwtService;
import com.prathameshAIcourceBuilder.AiCourceBuild.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @GetMapping("/google/success")
    public void googleAuthSuccess(@AuthenticationPrincipal OAuth2User oauth2User, 
                                 HttpServletResponse response) throws IOException {
        
        try {
            String googleId = oauth2User.getAttribute("sub");
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String profilePicture = oauth2User.getAttribute("picture");

            System.out.println("OAuth success for user: " + email);

            // Find or create user
            User user = userService.findOrCreateUser(googleId, email, name, profilePicture);
            
            // Generate JWT token
            String jwtToken = jwtService.generateToken(user);
            
            // Redirect to frontend with token as query parameter
            String redirectUrl = frontendUrl + "?token=" + jwtToken;
            System.out.println("Redirecting to: " + redirectUrl);
            
            response.sendRedirect(redirectUrl);
            
        } catch (Exception e) {
            System.err.println("Error in OAuth success handler: " + e.getMessage());
            e.printStackTrace();
            // Redirect with error
            response.sendRedirect(frontendUrl + "?error=auth_failed");
        }
    }

    @GetMapping("/google/failure")
    public void googleAuthFailure(HttpServletResponse response) throws IOException {
        System.out.println("OAuth authentication failed");
        response.sendRedirect(frontendUrl + "?error=auth_failed");
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            // Remove "Bearer " prefix
            String jwtToken = token.substring(7);
            
            if (!jwtService.validateToken(jwtToken)) {
                return ResponseEntity.status(401).build();
            }
            
            String email = jwtService.getEmailFromToken(jwtToken);
            User user = userService.findByEmail(email).orElse(null);
            
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
            UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getProfilePicture(),
                user.getRole().name()
            );
            
            return ResponseEntity.ok(userDTO);
            
        } catch (Exception e) {
            System.err.println("Error in /me endpoint: " + e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/login/google")
    public ResponseEntity<Map<String, String>> getGoogleLoginUrl() {
        Map<String, String> response = new HashMap<>();
        response.put("loginUrl", "/oauth2/authorization/google");
        return ResponseEntity.ok(response);
    }
}