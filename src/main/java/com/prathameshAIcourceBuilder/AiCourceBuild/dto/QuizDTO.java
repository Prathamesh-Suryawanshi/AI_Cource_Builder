package com.prathameshAIcourceBuilder.AiCourceBuild.dto;

import java.util.ArrayList;
import java.util.List;

public class QuizDTO {
    private Long id;
    private String question;
    private List<String> options = new ArrayList<>();
    private String answer;

    public QuizDTO() {}
    public QuizDTO(Long id, String question, List<String> options, String answer) {
        this.id = id; this.question = question; this.options = options; this.answer = answer;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}
