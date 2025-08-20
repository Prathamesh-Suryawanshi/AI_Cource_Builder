package com.prathameshAIcourceBuilder.AiCourceBuild.service;

import com.prathameshAIcourceBuilder.AiCourceBuild.entity.User;
import com.prathameshAIcourceBuilder.AiCourceBuild.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Value("${app.admin.emails}")
    private String adminEmailsString;

    public User findOrCreateUser(String googleId, String email, String name, String profilePicture) {
        Optional<User> existingUser = userRepository.findByGoogleId(googleId);
        
        if (existingUser.isPresent()) {

            User user = existingUser.get();
            user.setLastLogin(LocalDateTime.now());
            
        
            user.setName(name);
            user.setProfilePicture(profilePicture);
            
            return userRepository.save(user);
        } else {
      
            User newUser = new User(googleId, email, name, profilePicture);
            
         
            if (isAdminEmail(email)) {
                newUser.setRole(User.Role.ADMIN);
            }
            
            newUser.setLastLogin(LocalDateTime.now());
            return userRepository.save(newUser);
        }
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public boolean isAdminEmail(String email) {
        if (adminEmailsString == null || adminEmailsString.trim().isEmpty()) {
            return false;
        }
        
        List<String> adminEmails = Arrays.asList(adminEmailsString.split(","));
        return adminEmails.stream()
                .map(String::trim)
                .anyMatch(adminEmail -> adminEmail.equalsIgnoreCase(email));
    }

    public long getTotalUserCount() {
        return userRepository.count();
    }

    public long getAdminCount() {
        return userRepository.findAll().stream()
                .mapToLong(user -> user.getRole() == User.Role.ADMIN ? 1 : 0)
                .sum();
    }

    public long getStudentCount() {
        return userRepository.findAll().stream()
                .mapToLong(user -> user.getRole() == User.Role.STUDENT ? 1 : 0)
                .sum();
    }
}