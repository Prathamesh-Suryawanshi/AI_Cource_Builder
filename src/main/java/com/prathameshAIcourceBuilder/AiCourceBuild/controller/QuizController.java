package com.prathameshAIcourceBuilder.AiCourceBuild.controller;

import com.prathameshAIcourceBuilder.AiCourceBuild.dto.QuizAttemptDTO;
import com.prathameshAIcourceBuilder.AiCourceBuild.dto.QuizSubmissionDTO;
import com.prathameshAIcourceBuilder.AiCourceBuild.entity.Quiz;
import com.prathameshAIcourceBuilder.AiCourceBuild.entity.User;
import com.prathameshAIcourceBuilder.AiCourceBuild.entity.UserQuizAttempt;
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
@RequestMapping("/api/quiz")
public class QuizController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserQuizAttemptRepository userQuizAttemptRepository;

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitQuiz(
            @RequestHeader("Authorization") String token,
            @RequestBody QuizSubmissionDTO submission) {
        
        try {
   
            String jwtToken = token.substring(7);
            if (!jwtService.validateToken(jwtToken)) {
                return ResponseEntity.status(401).build();
            }
            
            String email = jwtService.getEmailFromToken(jwtToken);
            User user = userService.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
        
            Quiz quiz = quizRepository.findById(submission.getQuizId()).orElse(null);
            if (quiz == null) {
                return ResponseEntity.badRequest().build();
            }
            
     
            boolean isCorrect = quiz.getAnswer().trim().equalsIgnoreCase(submission.getSelectedAnswer().trim());
            

            UserQuizAttempt attempt = new UserQuizAttempt(user, quiz, submission.getSelectedAnswer(), isCorrect);
            userQuizAttemptRepository.save(attempt);
            
          
            Map<String, Object> response = new HashMap<>();
            response.put("correct", isCorrect);
            response.put("correctAnswer", quiz.getAnswer());
            response.put("message", isCorrect ? "Correct! Well done!" : "Incorrect. Try again!");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/my-history")
    public ResponseEntity<List<QuizAttemptDTO>> getMyQuizHistory(@RequestHeader("Authorization") String token) {
        try {
          
            String jwtToken = token.substring(7);
            if (!jwtService.validateToken(jwtToken)) {
                return ResponseEntity.status(401).build();
            }
            
            String email = jwtService.getEmailFromToken(jwtToken);
            User user = userService.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
         
            List<UserQuizAttempt> attempts = userQuizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(user.getId());
            List<QuizAttemptDTO> attemptDTOs = new ArrayList<>();
            
            for (UserQuizAttempt attempt : attempts) {
                QuizAttemptDTO dto = new QuizAttemptDTO(
                    attempt.getId(),
                    attempt.getQuiz().getQuestion(),
                    attempt.getSelectedAnswer(),
                    attempt.getQuiz().getAnswer(),
                    attempt.getIsCorrect(),
                    attempt.getAttemptedAt()
                );
                attemptDTOs.add(dto);
            }
            
            return ResponseEntity.ok(attemptDTOs);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/my-stats")
    public ResponseEntity<Map<String, Object>> getMyQuizStats(@RequestHeader("Authorization") String token) {
        try {
            // Validate token and get user
            String jwtToken = token.substring(7);
            if (!jwtService.validateToken(jwtToken)) {
                return ResponseEntity.status(401).build();
            }
            
            String email = jwtService.getEmailFromToken(jwtToken);
            User user = userService.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
          
            long totalAttempts = userQuizAttemptRepository.countTotalAttemptsByUserId(user.getId());
            long correctAnswers = userQuizAttemptRepository.countCorrectAnswersByUserId(user.getId());
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAttempts", totalAttempts);
            stats.put("correctAnswers", correctAnswers);
            stats.put("accuracy", totalAttempts > 0 ? (double) correctAnswers / totalAttempts * 100 : 0);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}