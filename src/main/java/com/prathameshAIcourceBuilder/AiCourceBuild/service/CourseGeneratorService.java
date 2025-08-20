package com.prathameshAIcourceBuilder.AiCourceBuild.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prathameshAIcourceBuilder.AiCourceBuild.entity.Course;
import com.prathameshAIcourceBuilder.AiCourceBuild.entity.Lesson;
import com.prathameshAIcourceBuilder.AiCourceBuild.entity.Quiz;
import com.prathameshAIcourceBuilder.AiCourceBuild.repository.CourseRepository;
import com.prathameshAIcourceBuilder.AiCourceBuild.repository.LessonRepository;
import com.prathameshAIcourceBuilder.AiCourceBuild.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class CourseGeneratorService {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final CourseRepository courseRepo;
    private final LessonRepository lessonRepo;
    private final QuizRepository quizRepo;
    private final YouTubeService youTubeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CourseGeneratorService(CourseRepository courseRepo, LessonRepository lessonRepo, 
                                QuizRepository quizRepo, YouTubeService youTubeService) {
        this.courseRepo = courseRepo;
        this.lessonRepo = lessonRepo;
        this.quizRepo = quizRepo;
        this.youTubeService = youTubeService;
    }

    public Course generateCourse(String topic) {
        System.out.println("=== Starting course generation for topic: " + topic + " ===");

        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            System.out.println("⚠️ Gemini API key is not configured!");
            return fallbackCourse(topic);
        }
        
        System.out.println("✅ Gemini API key found (length: " + geminiApiKey.length() + ")");

        try {
            Course course = generateCourseOutline(topic);

            generateLessonsAndQuizzes(course, topic);
            
            System.out.println("✅ Course generation completed successfully!");
            return course;
            
        } catch (Exception e) {
            System.err.println("❌ Error during course generation: " + e.getMessage());
            e.printStackTrace();
            return fallbackCourse(topic);
        }
    }

    public List<Course> getAllCourses() {
        return courseRepo.findAll();
    }

    public Optional<Course> getCourseById(Long id) {
        return courseRepo.findById(id);
    }

    public void deleteCourse(Long id) {
        courseRepo.deleteById(id);
    }

    private Course generateCourseOutline(String topic) throws Exception {
        System.out.println("🤖 Calling Gemini API for course outline...");
        
        String prompt = String.format(
            "Create a comprehensive beginner-friendly course outline on the topic: '%s'. " +
            "The course should have exactly 3 lessons with clear titles and descriptions. " +
            "Return ONLY a valid JSON object in this exact format: " +
            "{\"title\": \"Course Title\", \"description\": \"Course Description\", " +
            "\"lessons\": [{\"title\": \"Lesson 1 Title\", \"description\": \"Lesson 1 Description\"}, " +
            "{\"title\": \"Lesson 2 Title\", \"description\": \"Lesson 2 Description\"}, " +
            "{\"title\": \"Lesson 3 Title\", \"description\": \"Lesson 3 Description\"}]}", 
            topic
        );

        String response = callGeminiAPI(prompt);
        System.out.println("📄 Gemini Response: " + response);

        response = cleanJsonContent(response);
        
        JsonNode courseNode = objectMapper.readTree(response);

        Course course = new Course();
        course.setTitle(courseNode.get("title").asText());
        course.setDescription(courseNode.get("description").asText());
        course = courseRepo.save(course);
        
        System.out.println("✅ Course outline created: " + course.getTitle());
        return course;
    }

    private void generateLessonsAndQuizzes(Course course, String topic) throws Exception {
        System.out.println("📚 Generating lessons and quizzes...");

        String lessonPrompt = String.format(
            "For the course '%s' about '%s', create exactly 3 detailed lessons. " +
            "Each lesson should have comprehensive content that can be used for quiz generation. " +
            "Return ONLY a valid JSON array: " +
            "[{\"title\": \"Lesson 1 Title\", \"description\": \"Detailed lesson 1 description with key concepts\"}, " +
            "{\"title\": \"Lesson 2 Title\", \"description\": \"Detailed lesson 2 description with key concepts\"}, " +
            "{\"title\": \"Lesson 3 Title\", \"description\": \"Detailed lesson 3 description with key concepts\"}]",
            course.getTitle(), topic
        );

        String lessonsJson = callGeminiAPI(lessonPrompt);
        JsonNode lessonsNode = objectMapper.readTree(cleanJsonContent(lessonsJson));

        for (JsonNode lessonNode : lessonsNode) {
            Lesson lesson = new Lesson();
            lesson.setCourse(course);
            lesson.setTitle(lessonNode.get("title").asText());
            lesson.setDescription(lessonNode.get("description").asText());

            String videoUrl = youTubeService.searchVideoUrl(lesson.getTitle() + " " + topic);
            lesson.setVideoUrl(videoUrl != null ? videoUrl : "https://youtube.com");
            
            lesson = lessonRepo.save(lesson);
            System.out.println("✅ Created lesson: " + lesson.getTitle());

            generateQuizForLesson(lesson, topic);
        }
    }

    private void generateQuizForLesson(Lesson lesson, String topic) {
        try {
            System.out.println("❓ Generating quiz for lesson: " + lesson.getTitle());
            
            String quizPrompt = String.format(
                "Create a multiple choice quiz question for the lesson '%s' in a course about '%s'. " +
                "Base the question on the lesson description: '%s'. " +
                "The question should test practical understanding of the topic. " +
                "Make sure the options are realistic and educational, with only one clearly correct answer. " +
                "Return ONLY a valid JSON object: " +
                "{\"question\": \"A specific, practical question about the lesson content?\", " +
                "\"options\": [\"Realistic option A\", \"Realistic option B\", \"Realistic option C\", \"Realistic option D\"], " +
                "\"answer\": \"The exact text of the correct option\"}",
                lesson.getTitle(), topic, lesson.getDescription()
            );

            String quizJson = callGeminiAPI(quizPrompt);
            JsonNode quizNode = objectMapper.readTree(cleanJsonContent(quizJson));

            Quiz quiz = new Quiz();
            quiz.setLesson(lesson);
            quiz.setQuestion(quizNode.get("question").asText());

            List<String> options = new ArrayList<>();
            quizNode.get("options").forEach(opt -> options.add(opt.asText()));
            quiz.setOptionsJson(objectMapper.writeValueAsString(options));
            quiz.setAnswer(quizNode.get("answer").asText());
            
            quizRepo.save(quiz);
            System.out.println("✅ Created AI-generated quiz for lesson: " + lesson.getTitle());
            
        } catch (Exception e) {
            System.err.println("❌ Failed to generate AI quiz for lesson: " + lesson.getTitle());
            System.err.println("❌ Error: " + e.getMessage());
            createFallbackQuiz(lesson, topic);
        }
    }

    private String callGeminiAPI(String prompt) throws Exception {
        String url = geminiApiUrl + "?key=" + geminiApiKey;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            ),
            "generationConfig", Map.of(
                "temperature", 0.7,
                "maxOutputTokens", 1000,
                "topP", 0.8,
                "topK", 10
            )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Gemini API returned status: " + response.getStatusCode());
            }

            return extractContentFromGeminiResponse(response.getBody());
            
        } catch (Exception e) {
            System.err.println("❌ Gemini API call failed: " + e.getMessage());
            throw e;
        }
    }

    private String extractContentFromGeminiResponse(Map<String, Object> responseBody) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("No candidates in Gemini response");
            }
            
            Map<String, Object> firstCandidate = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            
            if (parts == null || parts.isEmpty()) {
                throw new RuntimeException("No parts in Gemini response content");
            }
            
            return (String) parts.get(0).get("text");
            
        } catch (Exception e) {
            System.err.println("❌ Error extracting content from Gemini response: " + e.getMessage());
            System.err.println("Response body: " + responseBody);
            throw new RuntimeException("Failed to extract content from Gemini response", e);
        }
    }

    private String cleanJsonContent(String content) {
        content = content.trim();
        if (content.startsWith("```json")) {
            content = content.substring(7);
        }
        if (content.startsWith("```")) {
            content = content.substring(3);
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }
        return content.trim();
    }

    private void createFallbackQuiz(Lesson lesson, String topic) {
        System.out.println("⚠️ Creating simple fallback quiz for: " + lesson.getTitle());
        
        Quiz quiz = new Quiz();
        quiz.setLesson(lesson);
        quiz.setQuestion("What is the main focus of " + lesson.getTitle() + "?");
        
        try {
            List<String> options = Arrays.asList(
                "Understanding " + topic + " fundamentals",
                "Learning advanced mathematics",
                "Database management",
                "Network configuration"
            );
            quiz.setOptionsJson(objectMapper.writeValueAsString(options));
        } catch (Exception e) {
            quiz.setOptionsJson("[\"Understanding fundamentals\", \"Advanced topics\", \"Database management\", \"Network configuration\"]");
        }
        
        quiz.setAnswer("Understanding " + topic + " fundamentals");
        quizRepo.save(quiz);
        
        System.out.println("✅ Created simple fallback quiz for lesson: " + lesson.getTitle());
    }

    private Course fallbackCourse(String topic) {
        System.out.println("⚠️ Using simple fallback course generation for topic: " + topic);
        
        Course course = new Course();
        course.setTitle("Introduction to " + topic);
        course.setDescription("A comprehensive beginner-friendly course covering the fundamentals of " + topic + ".");
        course = courseRepo.save(course);

        String[] lessonTitles = {
            "Getting Started with " + topic,
            "Core Concepts of " + topic,
            "Advanced " + topic + " Techniques"
        };

        String[] lessonDescriptions = {
            "Introduction and basic setup for " + topic + " with fundamental concepts.",
            "Understanding the fundamental principles, key features, and best practices of " + topic + ".",
            "Advanced techniques, real-world applications, and project implementation in " + topic + "."
        };

        for (int i = 0; i < 3; i++) {
            Lesson lesson = new Lesson();
            lesson.setCourse(course);
            lesson.setTitle(lessonTitles[i]);
            lesson.setDescription(lessonDescriptions[i]);
    
            String videoUrl = youTubeService.searchVideoUrl(lessonTitles[i]);
            lesson.setVideoUrl(videoUrl != null ? videoUrl : "https://youtube.com");
            
            lesson = lessonRepo.save(lesson);

            createFallbackQuiz(lesson, topic);
        }

        return course;
    }
}