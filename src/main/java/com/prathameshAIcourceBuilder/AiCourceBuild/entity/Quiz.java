package com.prathameshAIcourceBuilder.AiCourceBuild.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    @Column(columnDefinition = "TEXT")
    private String optionsJson;

    private String answer;

  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    @JsonBackReference
    private Lesson lesson;

    public Quiz() {}

    public Quiz(Lesson lesson, String question, String optionsJson, String answer) {
        this.lesson = lesson;
        this.question = question;
        this.optionsJson = optionsJson;
        this.answer = answer;
    }


    public Long getId() { return id; }
    public String getQuestion() { return question; }
    public String getOptionsJson() { return optionsJson; }
    public String getAnswer() { return answer; }
    public Lesson getLesson() { return lesson; }

    public void setId(Long id) { this.id = id; }
    public void setQuestion(String question) { this.question = question; }
    public void setOptionsJson(String optionsJson) { this.optionsJson = optionsJson; }
    public void setAnswer(String answer) { this.answer = answer; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }
}
