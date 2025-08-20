package com.prathameshAIcourceBuilder.AiCourceBuild.dto;

import java.util.ArrayList;
import java.util.List;

public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private List<LessonDTO> lessons = new ArrayList<>();

    public CourseDTO() {}
    public CourseDTO(Long id, String title, String description, List<LessonDTO> lessons) {
        this.id = id; this.title = title; this.description = description; this.lessons = lessons;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<LessonDTO> getLessons() { return lessons; }
    public void setLessons(List<LessonDTO> lessons) { this.lessons = lessons; }
}