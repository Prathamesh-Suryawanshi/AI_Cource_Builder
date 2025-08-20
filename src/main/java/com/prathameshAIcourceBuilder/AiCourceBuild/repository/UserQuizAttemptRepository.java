package com.prathameshAIcourceBuilder.AiCourceBuild.repository;

import com.prathameshAIcourceBuilder.AiCourceBuild.entity.UserQuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserQuizAttemptRepository extends JpaRepository<UserQuizAttempt, Long> {
    
    List<UserQuizAttempt> findByUserIdOrderByAttemptedAtDesc(Long userId);
    
    List<UserQuizAttempt> findByUserIdAndQuizId(Long userId, Long quizId);
    
    @Query("SELECT COUNT(uqa) FROM UserQuizAttempt uqa WHERE uqa.user.id = :userId AND uqa.isCorrect = true")
    long countCorrectAnswersByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(uqa) FROM UserQuizAttempt uqa WHERE uqa.user.id = :userId")
    long countTotalAttemptsByUserId(@Param("userId") Long userId);
}