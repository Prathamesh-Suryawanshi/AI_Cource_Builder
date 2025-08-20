package com.prathameshAIcourceBuilder.AiCourceBuild.dto;

import java.time.LocalDateTime;

public class QuizAttemptDTO {
    private Long id;
    private String question;
    private String selectedAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
    private LocalDateTime attemptedAt;

    public QuizAttemptDTO() {}

    public QuizAttemptDTO(Long id, String question, String selectedAnswer, 
                         String correctAnswer, Boolean isCorrect, LocalDateTime attemptedAt) {
        this.id = id;
        this.question = question;
        this.selectedAnswer = selectedAnswer;
        this.correctAnswer = correctAnswer;
        this.isCorrect = isCorrect;
        this.attemptedAt = attemptedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getSelectedAnswer() { return selectedAnswer; }
    public void setSelectedAnswer(String selectedAnswer) { this.selectedAnswer = selectedAnswer; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public LocalDateTime getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(LocalDateTime attemptedAt) { this.attemptedAt = attemptedAt; }
}