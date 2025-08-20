package com.prathameshAIcourceBuilder.AiCourceBuild.dto;

import java.util.ArrayList;
import java.util.List;

public class LessonDTO {
    private Long id;
    private String title;
    private String description;
    private String videoUrl;
    private List<QuizDTO> quizzes = new ArrayList<>();

    public LessonDTO() {}
    public LessonDTO(Long id, String title, String description, String videoUrl, List<QuizDTO> quizzes) {
        this.id = id; this.title = title; this.description = description; this.videoUrl = videoUrl; this.quizzes = quizzes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public List<QuizDTO> getQuizzes() { return quizzes; }
    public void setQuizzes(List<QuizDTO> quizzes) { this.quizzes = quizzes; }
}