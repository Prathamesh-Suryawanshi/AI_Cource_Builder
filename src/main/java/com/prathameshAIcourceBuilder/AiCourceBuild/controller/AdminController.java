package com.prathameshAIcourceBuilder.AiCourceBuild.controller;

import com.prathameshAIcourceBuilder.AiCourceBuild.dto.UserDTO;
import com.prathameshAIcourceBuilder.AiCourceBuild.entity.User;
import com.prathameshAIcourceBuilder.AiCourceBuild.repository.CourseRepository;
import com.prathameshAIcourceBuilder.AiCourceBuild.repository.LessonRepository;
import com.prathameshAIcourceBuilder.AiCourceBuild.repository.QuizRepository;
import com.prathameshAIcourceBuilder.AiCourceBuild.repository.UserQuizAttemptRepository;
import com.prathameshAIcourceBuilder.AiCourceBuild.service.JwtService;
import com.prathameshAIcourceBuilder.AiCourceBuild.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserQuizAttemptRepository userQuizAttemptRepository;

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestHeader("Authorization") String token) {
        if (!isAdmin(token)) {
            return ResponseEntity.status(403).build();
        }

        List<User> users = userService.findAllUsers();
        List<UserDTO> userDTOs = new ArrayList<>();

        for (User user : users) {
            UserDTO dto = new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getProfilePicture(),
                user.getRole().name()
            );
            userDTOs.add(dto);
        }

        return ResponseEntity.ok(userDTOs);
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @RequestHeader("Authorization") String token,
            @PathVariable Long userId) {
        
        if (!isAdmin(token)) {
            return ResponseEntity.status(403).build();
        }

        try {
            userService.deleteUser(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/course/{courseId}")
    public ResponseEntity<Map<String, String>> deleteCourse(
            @RequestHeader("Authorization") String token,
            @PathVariable Long courseId) {
        
        if (!isAdmin(token)) {
            return ResponseEntity.status(403).build();
        }

        try {
            courseRepository.deleteById(courseId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Course deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getSystemAnalytics(@RequestHeader("Authorization") String token) {
        if (!isAdmin(token)) {
            return ResponseEntity.status(403).build();
        }

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalUsers", userService.getTotalUserCount());
        analytics.put("totalStudents", userService.getStudentCount());
        analytics.put("totalAdmins", userService.getAdminCount());
        analytics.put("totalCourses", courseRepository.count());
        analytics.put("totalLessons", lessonRepository.count());
        analytics.put("totalQuizzes", quizRepository.count());
        analytics.put("totalQuizAttempts", userQuizAttemptRepository.count());

        return ResponseEntity.ok(analytics);
    }

    private boolean isAdmin(String token) {
        try {
            String jwtToken = token.substring(7);
            if (!jwtService.validateToken(jwtToken)) {
                return false;
            }
            
            String role = jwtService.getRoleFromToken(jwtToken);
            return "ADMIN".equals(role);
            
        } catch (Exception e) {
            return false;
        }
    }
}