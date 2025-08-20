package com.prathameshAIcourceBuilder.AiCourceBuild.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prathameshAIcourceBuilder.AiCourceBuild.dto.*;
import com.prathameshAIcourceBuilder.AiCourceBuild.entity.*;

import java.util.ArrayList;
import java.util.List;

public class DtoMapper {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static QuizDTO toQuizDTO(Quiz q) {
        List<String> options = new ArrayList<>();
        try {
            if (q.getOptionsJson() != null && !q.getOptionsJson().isBlank()) {
                options = MAPPER.readValue(q.getOptionsJson(), new TypeReference<List<String>>() {});
            }
        } catch (Exception ignored) {}
        return new QuizDTO(q.getId(), q.getQuestion(), options, q.getAnswer());
    }

    public static LessonDTO toLessonDTO(Lesson l, List<Quiz> quizzes) {
        List<QuizDTO> quizDTOs = new ArrayList<>();
        if (quizzes != null) {
            for (Quiz q : quizzes) quizDTOs.add(toQuizDTO(q));
        }
        return new LessonDTO(l.getId(), l.getTitle(), l.getDescription(), l.getVideoUrl(), quizDTOs);
    }

    public static CourseDTO toCourseDTO(Course c, List<LessonDTO> lessonDTOs) {
        return new CourseDTO(c.getId(), c.getTitle(), c.getDescription(), lessonDTOs);
    }
}
