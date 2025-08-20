package com.prathameshAIcourceBuilder.AiCourceBuild.repository;

import com.prathameshAIcourceBuilder.AiCourceBuild.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByLessonId(Long lessonId);
}