package com.prathameshAIcourceBuilder.AiCourceBuild.controller;

import com.prathameshAIcourceBuilder.AiCourceBuild.dto.CourseDTO;
import com.prathameshAIcourceBuilder.AiCourceBuild.dto.LessonDTO;
import com.prathameshAIcourceBuilder.AiCourceBuild.dto.QuizDTO;
import com.prathameshAIcourceBuilder.AiCourceBuild.entity.Course;
import com.prathameshAIcourceBuilder.AiCourceBuild.entity.Lesson;
import com.prathameshAIcourceBuilder.AiCourceBuild.entity.Quiz;
import com.prathameshAIcourceBuilder.AiCourceBuild.repository.CourseRepository;
import com.prathameshAIcourceBuilder.AiCourceBuild.repository.LessonRepository;
import com.prathameshAIcourceBuilder.AiCourceBuild.repository.QuizRepository;
import com.prathameshAIcourceBuilder.AiCourceBuild.service.CourseGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseGeneratorService courseService;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private QuizRepository quizRepository;

    @PostMapping("/generate")
    public CourseDTO generateCourse(@RequestParam("topic") String topic) {
        Course course = courseService.generateCourse(topic);
        
        List<Lesson> lessons = lessonRepository.findByCourseId(course.getId());
        List<LessonDTO> lessonDTOs = new ArrayList<>();
        
        for (Lesson lesson : lessons) {
            List<Quiz> quizzes = quizRepository.findByLessonId(lesson.getId());
            
            List<QuizDTO> quizDTOs = new ArrayList<>();
            for (Quiz quiz : quizzes) {
                QuizDTO quizDTO = convertToQuizDTO(quiz);
                quizDTOs.add(quizDTO);
            }
            
            LessonDTO lessonDTO = new LessonDTO(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getVideoUrl(),
                quizDTOs
            );
            
            lessonDTOs.add(lessonDTO);
        }
        
        CourseDTO courseDTO = new CourseDTO(
            course.getId(),
            course.getTitle(),
            course.getDescription(),
            lessonDTOs
        );
        
        return courseDTO;
    }

    @GetMapping("/all")
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        List<CourseDTO> courseDTOs = new ArrayList<>();
        
        for (Course course : courses) {
            List<Lesson> lessons = lessonRepository.findByCourseId(course.getId());
            List<LessonDTO> lessonDTOs = new ArrayList<>();
            
            for (Lesson lesson : lessons) {
                List<Quiz> quizzes = quizRepository.findByLessonId(lesson.getId());
                
                List<QuizDTO> quizDTOs = new ArrayList<>();
                for (Quiz quiz : quizzes) {
                    QuizDTO quizDTO = convertToQuizDTO(quiz);
                    quizDTOs.add(quizDTO);
                }
                
                LessonDTO lessonDTO = new LessonDTO(
                    lesson.getId(),
                    lesson.getTitle(),
                    lesson.getDescription(),
                    lesson.getVideoUrl(),
                    quizDTOs
                );
                
                lessonDTOs.add(lessonDTO);
            }
            
            CourseDTO courseDTO = new CourseDTO(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                lessonDTOs
            );
            
            courseDTOs.add(courseDTO);
        }
        
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        Optional<Course> courseOpt = courseRepository.findById(id);
        
        if (courseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Course course = courseOpt.get();
        List<Lesson> lessons = lessonRepository.findByCourseId(course.getId());
        List<LessonDTO> lessonDTOs = new ArrayList<>();
        
        for (Lesson lesson : lessons) {
            List<Quiz> quizzes = quizRepository.findByLessonId(lesson.getId());
            
            List<QuizDTO> quizDTOs = new ArrayList<>();
            for (Quiz quiz : quizzes) {
                QuizDTO quizDTO = convertToQuizDTO(quiz);
                quizDTOs.add(quizDTO);
            }
            
            LessonDTO lessonDTO = new LessonDTO(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getVideoUrl(),
                quizDTOs
            );
            
            lessonDTOs.add(lessonDTO);
        }
        
        CourseDTO courseDTO = new CourseDTO(
            course.getId(),
            course.getTitle(),
            course.getDescription(),
            lessonDTOs
        );
        
        return ResponseEntity.ok(courseDTO);
    }
    
    private QuizDTO convertToQuizDTO(Quiz quiz) {
        List<String> options = new ArrayList<>();
        
        try {
            if (quiz.getOptionsJson() != null && !quiz.getOptionsJson().isBlank()) {
                String optionsJson = quiz.getOptionsJson();
                
                optionsJson = optionsJson.replace("[", "").replace("]", "");
                String[] optionArray = optionsJson.split("\",\"");
                
                for (String option : optionArray) {
                    option = option.replace("\"", "").trim();
                    options.add(option);
                }
            }
        } catch (Exception e) {
            options.add("Option A");
            options.add("Option B");
            options.add("Option C");
            options.add("Option D");
        }
        
        return new QuizDTO(
            quiz.getId(),
            quiz.getQuestion(),
            options,
            quiz.getAnswer()
        );
    }
}