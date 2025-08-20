package com.prathameshAIcourceBuilder.AiCourceBuild.dto;

public class QuizSubmissionDTO {
    private Long quizId;
    private String selectedAnswer;

    public QuizSubmissionDTO() {}

    public QuizSubmissionDTO(Long quizId, String selectedAnswer) {
        this.quizId = quizId;
        this.selectedAnswer = selectedAnswer;
    }

   
    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public String getSelectedAnswer() { return selectedAnswer; }
    public void setSelectedAnswer(String selectedAnswer) { this.selectedAnswer = selectedAnswer; }
}