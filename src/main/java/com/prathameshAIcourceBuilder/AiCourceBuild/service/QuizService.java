package com.prathameshAIcourceBuilder.AiCourceBuild.service;

import org.springframework.stereotype.Service;

@Service
public class QuizService {

    public String generateSimpleQuestion(String lessonTitle) {
        
        return "What is the primary purpose of " + lessonTitle + "?";
    }
}
