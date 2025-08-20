import { useState, useEffect } from "react";
import axios from "axios";

function App() {
  const [topic, setTopic] = useState("");
  const [course, setCourse] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [progress, setProgress] = useState(0);
  
  const [quizAnswers, setQuizAnswers] = useState({});
  const [quizSubmitted, setQuizSubmitted] = useState({});
  const [quizResults, setQuizResults] = useState({});
  const [topics, setTopics] = useState([]);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

  // Authentication state
  const [user, setUser] = useState(null);
  const [authLoading, setAuthLoading] = useState(true);
  const [allCourses, setAllCourses] = useState([]);
  const [quizStats, setQuizStats] = useState(null);
  const [token, setToken] = useState(null);

  // Admin state
  const [currentView, setCurrentView] = useState('dashboard');
  const [adminData, setAdminData] = useState({
    users: [],
    courses: [],
    analytics: {}
  });
  const [adminLoading, setAdminLoading] = useState(false);

  const api = "http://localhost:8080/api/course/generate";
  const authApi = "http://localhost:8080/api/auth";
  const adminApi = "http://localhost:8080/api/admin";

  // Set up axios interceptor for authentication
  useEffect(() => {
    const requestInterceptor = axios.interceptors.request.use(
      (config) => {
        const currentToken = localStorage.getItem('authToken');
        if (currentToken) {
          config.headers.Authorization = `Bearer ${currentToken}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    const responseInterceptor = axios.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          localStorage.removeItem('authToken');
          setToken(null);
          setUser(null);
        }
        return Promise.reject(error);
      }
    );

    return () => {
      axios.interceptors.request.eject(requestInterceptor);
      axios.interceptors.response.eject(responseInterceptor);
    };
  }, []);

  // Check authentication status on mount
  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const tokenFromUrl = urlParams.get('token');
    const errorFromUrl = urlParams.get('error');

    if (tokenFromUrl) {
      localStorage.setItem('authToken', tokenFromUrl);
      setToken(tokenFromUrl);
      
      const cleanUrl = window.location.origin + window.location.pathname;
      window.history.replaceState({}, document.title, cleanUrl);
      
      checkAuthStatus();
    } else if (errorFromUrl) {
      setError('Authentication failed. Please try again.');
      setAuthLoading(false);
      
      const cleanUrl = window.location.origin + window.location.pathname;
      window.history.replaceState({}, document.title, cleanUrl);
    } else {
      const storedToken = localStorage.getItem('authToken');
      if (storedToken) {
        setToken(storedToken);
        checkAuthStatus();
      } else {
        setAuthLoading(false);
      }
    }
  }, []);

  const checkAuthStatus = async () => {
    try {
      const currentToken = localStorage.getItem('authToken');
      if (!currentToken) {
        setAuthLoading(false);
        return;
      }

      const response = await axios.get(`${authApi}/me`, {
        headers: {
          Authorization: `Bearer ${currentToken}`
        }
      });
      
      setUser(response.data);
      loadUserData();
    } catch (error) {
      localStorage.removeItem('authToken');
      setToken(null);
      setUser(null);
    } finally {
      setAuthLoading(false);
    }
  };

  const loadUserData = async () => {
    try {
      const currentToken = localStorage.getItem('authToken');
      const config = {
        headers: {
          Authorization: `Bearer ${currentToken}`
        }
      };

      const coursesResponse = await axios.get("http://localhost:8080/api/course/all", config);
      setAllCourses(coursesResponse.data);

      const statsResponse = await axios.get("http://localhost:8080/api/quiz/my-stats", config);
      setQuizStats(statsResponse.data);
    } catch (error) {
      console.error("Error loading user data:", error);
    }
  };

  // Admin functions
  const loadAdminData = async () => {
    if (user?.role !== 'ADMIN') return;
    
    setAdminLoading(true);
    try {
      const currentToken = localStorage.getItem('authToken');
      const config = {
        headers: {
          Authorization: `Bearer ${currentToken}`
        }
      };

      const [usersRes, coursesRes, analyticsRes] = await Promise.all([
        axios.get(`${adminApi}/users`, config),
        axios.get("http://localhost:8080/api/course/all", config),
        axios.get(`${adminApi}/analytics`, config)
      ]);

      setAdminData({
        users: usersRes.data,
        courses: coursesRes.data,
        analytics: analyticsRes.data
      });
    } catch (error) {
      setError("Failed to load admin data");
    } finally {
      setAdminLoading(false);
    }
  };

  const deleteUser = async (userId) => {
    if (!window.confirm('Are you sure you want to delete this user?')) return;
    
    try {
      const currentToken = localStorage.getItem('authToken');
      await axios.delete(`${adminApi}/user/${userId}`, {
        headers: {
          Authorization: `Bearer ${currentToken}`
        }
      });
      
      loadAdminData(); // Refresh data
    } catch (error) {
      setError("Failed to delete user");
    }
  };

  const deleteCourse = async (courseId) => {
    if (!window.confirm('Are you sure you want to delete this course?')) return;
    
    try {
      const currentToken = localStorage.getItem('authToken');
      await axios.delete(`${adminApi}/course/${courseId}`, {
        headers: {
          Authorization: `Bearer ${currentToken}`
        }
      });
      
      loadAdminData(); // Refresh data
    } catch (error) {
      setError("Failed to delete course");
    }
  };

  // Load admin data when switching to admin view
  useEffect(() => {
    if (currentView.startsWith('admin') && user?.role === 'ADMIN') {
      loadAdminData();
    }
  }, [currentView, user]);

  const handleLogin = () => {
    const currentUrl = window.location.origin;
    localStorage.setItem('redirectAfterLogin', currentUrl);
    window.location.href = `http://localhost:8080/oauth2/authorization/google`;
  };

  const handleLogout = async () => {
    try {
      await axios.post(`${authApi}/logout`);
    } catch (error) {
      console.error("Logout error:", error);
    } finally {
      localStorage.removeItem('authToken');
      setToken(null);
      setUser(null);
      setCourse(null);
      setQuizAnswers({});
      setQuizSubmitted({});
      setQuizResults({});
      setTopics([]);
      setAllCourses([]);
      setQuizStats(null);
      setCurrentView('dashboard');
    }
  };

  // Simulate progress during loading
  useEffect(() => {
    if (loading) {
      setProgress(0);
      const interval = setInterval(() => {
        setProgress(prev => {
          if (prev >= 90) return prev;
          return prev + Math.random() * 10;
        });
      }, 500);
      
      return () => clearInterval(interval);
    } else {
      setProgress(0);
    }
  }, [loading]);

  const generateCourse = async () => {
    if (!user) {
      setError("Please login to generate courses");
      return;
    }
    
    if (!topic.trim()) {
      setError("Please enter a topic");
      return;
    }
    
    setCurrentView('dashboard'); // Switch back to main view
    setLoading(true);
    setError("");
    setCourse(null);
    setQuizAnswers({});
    setQuizSubmitted({});
    setQuizResults({});
    setProgress(0);
    
    try {
      const currentToken = localStorage.getItem('authToken');
      const res = await axios.post(
        `${api}?topic=${encodeURIComponent(topic)}`,
        {},
        { 
          headers: {
            Authorization: `Bearer ${currentToken}`
          },
          timeout: 30000 
        }
      );
      setProgress(100);
      setTimeout(() => {
        setCourse(res.data);
        setTopics((prev) => (prev.includes(topic) ? prev : [...prev, topic]));
        loadUserData();
      }, 300);
    } catch (err) {
      if (err.code === "ECONNABORTED") {
        setError("Request timeout. The course generation is taking too long.");
      } else if (err.response?.status === 401) {
        setError("Please login to generate courses.");
        localStorage.removeItem('authToken');
        setToken(null);
        setUser(null);
      } else if (err.response) {
        setError(`Server error: ${err.response.status} - ${err.response.statusText}`);
      } else if (err.request) {
        setError("Network error. Please check if the backend server is running on port 8080.");
      } else {
        setError("Failed to generate course. Please try again.");
      }
    } finally {
      setTimeout(() => setLoading(false), 500);
    }
  };

  const submitQuizAnswer = async (quizId, correctAnswer, questionText, options) => {
    const selectedAnswer = quizAnswers[quizId];
    if (!selectedAnswer || !user) return;
    
    setQuizSubmitted((prev) => ({ ...prev, [quizId]: true }));
    const isCorrect = selectedAnswer === correctAnswer;
    
    setQuizResults((prev) => ({
      ...prev,
      [quizId]: {
        selected: selectedAnswer,
        correct: correctAnswer,
        isCorrect: isCorrect,
      },
    }));

    try {
      const currentToken = localStorage.getItem('authToken');
      await axios.post("http://localhost:8080/api/quiz/submit", {
        courseId: course.id,
        question: questionText,
        selectedAnswer: selectedAnswer,
        correctAnswer: correctAnswer,
        isCorrect: isCorrect,
        options: options
      }, {
        headers: {
          Authorization: `Bearer ${currentToken}`
        }
      });
      
      const statsResponse = await axios.get("http://localhost:8080/api/quiz/my-stats", {
        headers: {
          Authorization: `Bearer ${currentToken}`
        }
      });
      setQuizStats(statsResponse.data);
    } catch (error) {
      console.error("Error submitting quiz:", error);
    }
  };

  const loadCourseById = async (courseId) => {
    try {
      setLoading(true);
      setCurrentView('dashboard');
      const currentToken = localStorage.getItem('authToken');
      const response = await axios.get(`http://localhost:8080/api/course/${courseId}`, {
        headers: {
          Authorization: `Bearer ${currentToken}`
        }
      });
      setCourse(response.data);
      setTopic(response.data.title);
    } catch (error) {
      setError("Failed to load course");
    } finally {
      setLoading(false);
    }
  };

  const embedUrl = (url) => {
    if (!url) return null;
    if (url.includes("youtube.com/watch?v=")) {
      return url.replace("watch?v=", "embed/");
    } else if (url.includes("youtu.be/")) {
      const videoId = url.split("youtu.be/")[1].split("?")[0];
      return `https://www.youtube.com/embed/${videoId}`;
    }
    return url;
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter") generateCourse();
  };

  const handleQuizOptionSelect = (quizId, selectedOption) => {
    if (quizSubmitted[quizId]) return;
    setQuizAnswers((prev) => ({ ...prev, [quizId]: selectedOption }));
  };

  const resetQuiz = (quizId) => {
    setQuizAnswers((p) => { const x = { ...p }; delete x[quizId]; return x; });
    setQuizSubmitted((p) => { const x = { ...p }; delete x[quizId]; return x; });
    setQuizResults((p) => { const x = { ...p }; delete x[quizId]; return x; });
  };

  // Admin Components
  const AdminAnalytics = () => (
    <div className="glass-card p-5 shadow-glow">
      <div className="d-flex align-items-center mb-4">
        <div className="rounded-circle d-flex align-items-center justify-content-center me-3" style={{
          width: '60px', height: '60px',
          background: 'linear-gradient(135deg, #667eea, #764ba2)'
        }}>
          <i className="fas fa-chart-bar text-white fa-lg"></i>
        </div>
        <div>
          <h2 className="text-white mb-1">System Analytics</h2>
          <p className="text-white-75 mb-0">Overview of platform statistics</p>
        </div>
      </div>

      {adminLoading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-white mb-3"></div>
          <p className="text-white">Loading analytics...</p>
        </div>
      ) : (
        <div className="row g-4">
          <div className="col-md-3">
            <div className="glass-card p-4 text-center">
              <div className="rounded-circle d-inline-flex align-items-center justify-content-center mb-3" style={{
                width: '60px', height: '60px',
                background: 'linear-gradient(135deg, #4facfe, #00f2fe)'
              }}>
                <i className="fas fa-users text-white fa-lg"></i>
              </div>
              <h3 className="text-white mb-2">{adminData.analytics.totalUsers || 0}</h3>
              <p className="text-white-75 mb-0">Total Users</p>
            </div>
          </div>
          <div className="col-md-3">
            <div className="glass-card p-4 text-center">
              <div className="rounded-circle d-inline-flex align-items-center justify-content-center mb-3" style={{
                width: '60px', height: '60px',
                background: 'linear-gradient(135deg, #fa709a, #fee140)'
              }}>
                <i className="fas fa-graduation-cap text-white fa-lg"></i>
              </div>
              <h3 className="text-white mb-2">{adminData.analytics.totalCourses || 0}</h3>
              <p className="text-white-75 mb-0">Total Courses</p>
            </div>
          </div>
          <div className="col-md-3">
            <div className="glass-card p-4 text-center">
              <div className="rounded-circle d-inline-flex align-items-center justify-content-center mb-3" style={{
                width: '60px', height: '60px',
                background: 'linear-gradient(135deg, #a8edea, #fed6e3)'
              }}>
                <i className="fas fa-book text-white fa-lg"></i>
              </div>
              <h3 className="text-white mb-2">{adminData.analytics.totalLessons || 0}</h3>
              <p className="text-white-75 mb-0">Total Lessons</p>
            </div>
          </div>
          <div className="col-md-3">
            <div className="glass-card p-4 text-center">
              <div className="rounded-circle d-inline-flex align-items-center justify-content-center mb-3" style={{
                width: '60px', height: '60px',
                background: 'linear-gradient(135deg, #ffecd2, #fcb69f)'
              }}>
                <i className="fas fa-brain text-white fa-lg"></i>
              </div>
              <h3 className="text-white mb-2">{adminData.analytics.totalQuizAttempts || 0}</h3>
              <p className="text-white-75 mb-0">Quiz Attempts</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );

  const AdminUsers = () => (
    <div className="glass-card p-5 shadow-glow">
      <div className="d-flex align-items-center justify-content-between mb-4">
        <div className="d-flex align-items-center">
          <div className="rounded-circle d-flex align-items-center justify-content-center me-3" style={{
            width: '60px', height: '60px',
            background: 'linear-gradient(135deg, #667eea, #764ba2)'
          }}>
            <i className="fas fa-users text-white fa-lg"></i>
          </div>
          <div>
            <h2 className="text-white mb-1">User Management</h2>
            <p className="text-white-75 mb-0">Manage platform users</p>
          </div>
        </div>
        <div className="badge rounded-pill" style={{ background: 'rgba(255,255,255,0.2)', color: 'white', fontSize: '1rem' }}>
          {adminData.users.length} Users
        </div>
      </div>

      {adminLoading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-white mb-3"></div>
          <p className="text-white">Loading users...</p>
        </div>
      ) : (
        <div className="table-responsive">
          <table className="table table-dark table-hover">
            <thead>
              <tr>
                <th>User</th>
                <th>Email</th>
                <th>Role</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {adminData.users.map((user) => (
                <tr key={user.id}>
                  <td>
                    <div className="d-flex align-items-center">
                      <img 
                        src={user.profilePicture || '/api/placeholder/40/40'} 
                        alt={user.name}
                        className="rounded-circle me-3"
                        style={{ width: '40px', height: '40px' }}
                      />
                      <div>
                        <div className="fw-bold">{user.name}</div>
                        <small className="text-white-50">ID: {user.id}</small>
                      </div>
                    </div>
                  </td>
                  <td>{user.email}</td>
                  <td>
                    <span className={`badge rounded-pill ${user.role === 'ADMIN' ? 'bg-warning text-dark' : 'bg-info'}`}>
                      {user.role}
                    </span>
                  </td>
                  <td>
                    {user.role !== 'ADMIN' && (
                      <button 
                        className="btn btn-outline-danger btn-sm"
                        onClick={() => deleteUser(user.id)}
                      >
                        <i className="fas fa-trash me-1"></i>Delete
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );

  const AdminCourses = () => (
    <div className="glass-card p-5 shadow-glow">
      <div className="d-flex align-items-center justify-content-between mb-4">
        <div className="d-flex align-items-center">
          <div className="rounded-circle d-flex align-items-center justify-content-center me-3" style={{
            width: '60px', height: '60px',
            background: 'linear-gradient(135deg, #667eea, #764ba2)'
          }}>
            <i className="fas fa-graduation-cap text-white fa-lg"></i>
          </div>
          <div>
            <h2 className="text-white mb-1">Course Management</h2>
            <p className="text-white-75 mb-0">Manage platform courses</p>
          </div>
        </div>
        <div className="badge rounded-pill" style={{ background: 'rgba(255,255,255,0.2)', color: 'white', fontSize: '1rem' }}>
          {adminData.courses.length} Courses
        </div>
      </div>

      {adminLoading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-white mb-3"></div>
          <p className="text-white">Loading courses...</p>
        </div>
      ) : (
        <div className="row g-4">
          {adminData.courses.map((course) => (
            <div key={course.id} className="col-md-6 col-lg-4">
              <div className="glass-card p-4 h-100 d-flex flex-column">
                <div className="d-flex align-items-center mb-3">
                  <div className="rounded-circle d-flex align-items-center justify-content-center me-3" style={{
                    width: '50px', height: '50px',
                    background: 'linear-gradient(135deg, #4facfe, #00f2fe)'
                  }}>
                    <i className="fas fa-book text-white"></i>
                  </div>
                  <div className="flex-grow-1">
                    <h5 className="text-white mb-1">{course.title}</h5>
                    <small className="text-white-50">ID: {course.id}</small>
                  </div>
                </div>
                
                {course.description && (
                  <p className="text-white-75 mb-3 flex-grow-1" style={{ 
                    fontSize: '0.9rem',
                    display: '-webkit-box',
                    WebkitLineClamp: 3,
                    WebkitBoxOrient: 'vertical',
                    overflow: 'hidden'
                  }}>
                    {course.description}
                  </p>
                )}
                
                <div className="d-flex align-items-center justify-content-between mb-3">
                  <span className="badge rounded-pill" style={{ background: 'rgba(255,255,255,0.2)' }}>
                    {course.lessons?.length || 0} Lessons
                  </span>
                </div>
                
                <div className="d-flex gap-2">
                  <button 
                    className="btn btn-outline-primary btn-sm flex-grow-1"
                    onClick={() => loadCourseById(course.id)}
                  >
                    <i className="fas fa-eye me-1"></i>View
                  </button>
                  <button 
                    className="btn btn-outline-danger btn-sm"
                    onClick={() => deleteCourse(course.id)}
                  >
                    <i className="fas fa-trash"></i>
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );

  const InteractiveQuiz = ({ quiz, quizIndex, lessonIndex }) => {
    const quizId = `lesson-${lessonIndex}-quiz-${quizIndex}`;
    const selectedAnswer = quizAnswers[quizId];
    const isSubmitted = quizSubmitted[quizId];
    const result = quizResults[quizId];

    return (
      <div className="glass-card p-4 mb-4 position-relative overflow-hidden">
        <div className="position-absolute top-0 start-0 w-100 h-1 bg-gradient" style={{
          background: 'linear-gradient(90deg, #667eea, #764ba2)',
          height: '3px'
        }}></div>
        
        <div className="d-flex align-items-center mb-3">
          <div className="rounded-circle d-flex align-items-center justify-content-center me-3" style={{
            width: '40px', height: '40px',
            background: 'linear-gradient(135deg, #667eea, #764ba2)',
            color: 'white'
          }}>
            <i className="fas fa-question"></i>
          </div>
          <h6 className="fw-bold text-dark mb-0 flex-grow-1">{quiz.question}</h6>
        </div>

        {quiz.options?.length ? (
          <div className="mt-3">
            {quiz.options.map((option, optIndex) => {
              let optionClass = "list-group-item list-group-item-action mb-2 border-0 rounded-3 position-relative overflow-hidden";
              let bgStyle = {};
              
              if (isSubmitted && result) {
                if (option === quiz.answer) {
                  optionClass += " text-white";
                  bgStyle.background = "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)";
                } else if (option === selectedAnswer) {
                  optionClass += " text-white";
                  bgStyle.background = "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)";
                } else {
                  bgStyle.background = "rgba(248, 249, 250, 0.8)";
                }
              } else if (selectedAnswer === option) {
                optionClass += " text-white";
                bgStyle.background = "linear-gradient(135deg, #667eea 0%, #764ba2 100%)";
              } else {
                bgStyle.background = "rgba(248, 249, 250, 0.8)";
              }

              return (
                <div
                  key={optIndex}
                  className={optionClass}
                  style={{ 
                    cursor: isSubmitted ? "default" : "pointer",
                    ...bgStyle,
                    transition: "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)",
                    backdropFilter: "blur(10px)"
                  }}
                  onClick={() => handleQuizOptionSelect(quizId, option)}
                  onMouseEnter={(e) => {
                    if (!isSubmitted) {
                      e.currentTarget.style.transform = "translateX(8px) scale(1.02)";
                    }
                  }}
                  onMouseLeave={(e) => {
                    if (!isSubmitted) {
                      e.currentTarget.style.transform = "translateX(0) scale(1)";
                    }
                  }}
                >
                  <div className="d-flex align-items-center p-3">
                    <span className="badge rounded-circle d-flex align-items-center justify-content-center me-3" style={{
                      width: '32px', height: '32px',
                      background: selectedAnswer === option || (isSubmitted && option === quiz.answer) ? 
                        'rgba(255, 255, 255, 0.2)' : 'rgba(0, 0, 0, 0.1)'
                    }}>
                      {String.fromCharCode(65 + optIndex)}
                    </span>
                    <span className="flex-grow-1 fw-medium">{option}</span>
                    {isSubmitted && option === quiz.answer && (
                      <i className="fas fa-check-circle text-white"></i>
                    )}
                    {isSubmitted && option === selectedAnswer && option !== quiz.answer && (
                      <i className="fas fa-times-circle text-white"></i>
                    )}
                  </div>
                </div>
              );
            })}

            <div className="mt-4 d-flex justify-content-between align-items-center">
              {!isSubmitted ? (
                <button
                  className="btn btn-primary px-4 py-2 rounded-pill"
                  disabled={!selectedAnswer || !user}
                  onClick={() => submitQuizAnswer(quizId, quiz.answer, quiz.question, quiz.options)}
                  style={{
                    background: selectedAnswer && user ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' : 'rgba(0,0,0,0.1)',
                    border: 'none',
                    transition: 'all 0.3s ease'
                  }}
                >
                  <i className="fas fa-paper-plane me-2"></i>Submit Answer
                </button>
              ) : (
                <div className="d-flex align-items-center">
                  {result?.isCorrect ? (
                    <div className="d-flex align-items-center text-success">
                      <i className="fas fa-check-circle me-2 fs-5"></i>
                      <span className="fw-bold">Excellent! Correct answer!</span>
                    </div>
                  ) : (
                    <div className="d-flex align-items-center text-danger">
                      <i className="fas fa-times-circle me-2 fs-5"></i>
                      <span className="fw-bold">Incorrect. Correct answer: {quiz.answer}</span>
                    </div>
                  )}
                </div>
              )}
              {isSubmitted && (
                <button 
                  className="btn btn-outline-secondary rounded-pill px-3" 
                  onClick={() => resetQuiz(quizId)}
                  style={{ borderWidth: '2px' }}
                >
                  <i className="fas fa-redo me-2"></i>Try Again
                </button>
              )}
            </div>
          </div>
        ) : (
          <div className="alert alert-warning border-0 rounded-3">
            <i className="fas fa-exclamation-triangle me-2"></i>No options available
          </div>
        )}
      </div>
    );
  };

  // Show loading screen while checking authentication
  if (authLoading) {
    return (
      <div className="d-flex align-items-center justify-content-center min-vh-100 animated-bg">
        <div className="text-center">
          <div className="spinner-border text-white mb-3" style={{ width: "3rem", height: "3rem" }}></div>
          <h5 className="text-white">Loading...</h5>
        </div>
      </div>
    );
  }

  // Show login screen if not authenticated
  if (!user) {
    return (
      <div className="min-vh-100 d-flex align-items-center justify-content-center animated-bg">
        <div className="text-center">
          <div className="glass-card p-5 shadow-glow" style={{ maxWidth: '400px' }}>
            <div className="mb-4">
              <div className="rounded-circle d-inline-flex align-items-center justify-content-center mb-3" style={{
                width: '80px', height: '80px',
                background: 'linear-gradient(135deg, #667eea, #764ba2)'
              }}>
                <i className="fas fa-robot fa-2x text-white"></i>
              </div>
              <h2 className="text-white mb-2">AI Course Builder</h2>
              <p className="text-white-75 mb-4">
                Generate personalized learning courses with AI-powered content and interactive quizzes
              </p>
            </div>
            
            {error && (
              <div className="alert alert-danger mb-3" role="alert">
                {error}
              </div>
            )}
            
            <button 
              className="btn btn-primary btn-lg w-100 rounded-pill py-3"
              onClick={handleLogin}
              style={{
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                border: 'none',
                fontSize: '1.1rem'
              }}
            >
              <i className="fab fa-google me-3"></i>
              Continue with Google
            </button>
            
            <small className="text-white-50 mt-3 d-block">
              Secure authentication powered by Google OAuth
            </small>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="d-flex min-vh-100 animated-bg">
      {/* Sidebar */}
      <div
        className={`sidebar position-fixed h-100 transition-all duration-300 ${sidebarCollapsed ? 'collapsed' : ''}`}
        style={{
          width: sidebarCollapsed ? "80px" : "320px",
          left: 0,
          top: 0,
          zIndex: 1000,
          transition: "width 0.3s cubic-bezier(0.4, 0, 0.2, 1)",
          background: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)'
        }}
      >
        <div className="p-4 h-100 d-flex flex-column">
          <div className="d-flex justify-content-between align-items-center mb-4">
            {!sidebarCollapsed && (
              <div className="d-flex align-items-center">
                <img 
                  src={user.profilePicture} 
                  alt={user.name}
                  className="rounded-circle me-3"
                  style={{ width: '40px', height: '40px' }}
                />
                <div>
                  <div className="text-white fw-bold small">{user.name}</div>
                  <div className="text-white-50" style={{ fontSize: '0.75rem' }}>{user.email}</div>
                  {user.role === 'ADMIN' && (
                    <span className="badge bg-warning text-dark" style={{ fontSize: '0.6rem' }}>ADMIN</span>
                  )}
                </div>
              </div>
            )}
            <button 
              className="btn btn-link text-white p-0 border-0"
              onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
            >
              <i className={`fas ${sidebarCollapsed ? 'fa-chevron-right' : 'fa-chevron-left'}`}></i>
            </button>
          </div>
          
          {!sidebarCollapsed && (
            <>
              {/* Navigation Menu */}
              <div className="mb-4">
                <div className="d-flex align-items-center mb-3">
                  <div className="rounded-circle d-flex align-items-center justify-content-center me-3" style={{
                    width: '40px', height: '40px',
                    background: 'linear-gradient(135deg, #667eea, #764ba2)'
                  }}>
                    <i className="fas fa-compass text-white"></i>
                  </div>
                  <div>
                    <h6 className="text-white mb-0 fw-bold">Navigation</h6>
                    <small className="text-white-50">Main Menu</small>
                  </div>
                </div>

                <div className="list-group list-group-flush">
                  <button
                    className={`list-group-item bg-transparent text-white border-0 px-3 py-3 rounded-3 mb-2 position-relative overflow-hidden ${currentView === 'dashboard' ? 'active' : ''}`}
                    style={{ 
                      cursor: "pointer",
                      background: currentView === 'dashboard' ? "rgba(255, 255, 255, 0.2)" : "rgba(255, 255, 255, 0.1)",
                      backdropFilter: "blur(10px)",
                      transition: "all 0.3s ease"
                    }}
                    onClick={() => setCurrentView('dashboard')}
                  >
                    <div className="d-flex align-items-center">
                      <i className="fas fa-home me-3 text-primary"></i>
                      <span className="fw-medium">Dashboard</span>
                    </div>
                  </button>

                  {user?.role === 'ADMIN' && (
                    <>
                      <button
                        className={`list-group-item bg-transparent text-white border-0 px-3 py-3 rounded-3 mb-2 position-relative overflow-hidden ${currentView === 'admin-analytics' ? 'active' : ''}`}
                        style={{ 
                          cursor: "pointer",
                          background: currentView === 'admin-analytics' ? "rgba(255, 255, 255, 0.2)" : "rgba(255, 255, 255, 0.1)",
                          backdropFilter: "blur(10px)",
                          transition: "all 0.3s ease"
                        }}
                        onClick={() => setCurrentView('admin-analytics')}
                      >
                        <div className="d-flex align-items-center">
                          <i className="fas fa-chart-bar me-3 text-warning"></i>
                          <span className="fw-medium">Analytics</span>
                        </div>
                      </button>

                      <button
                        className={`list-group-item bg-transparent text-white border-0 px-3 py-3 rounded-3 mb-2 position-relative overflow-hidden ${currentView === 'admin-users' ? 'active' : ''}`}
                        style={{ 
                          cursor: "pointer",
                          background: currentView === 'admin-users' ? "rgba(255, 255, 255, 0.2)" : "rgba(255, 255, 255, 0.1)",
                          backdropFilter: "blur(10px)",
                          transition: "all 0.3s ease"
                        }}
                        onClick={() => setCurrentView('admin-users')}
                      >
                        <div className="d-flex align-items-center">
                          <i className="fas fa-users me-3 text-info"></i>
                          <span className="fw-medium">Users</span>
                        </div>
                      </button>

                      <button
                        className={`list-group-item bg-transparent text-white border-0 px-3 py-3 rounded-3 mb-2 position-relative overflow-hidden ${currentView === 'admin-courses' ? 'active' : ''}`}
                        style={{ 
                          cursor: "pointer",
                          background: currentView === 'admin-courses' ? "rgba(255, 255, 255, 0.2)" : "rgba(255, 255, 255, 0.1)",
                          backdropFilter: "blur(10px)",
                          transition: "all 0.3s ease"
                        }}
                        onClick={() => setCurrentView('admin-courses')}
                      >
                        <div className="d-flex align-items-center">
                          <i className="fas fa-graduation-cap me-3 text-success"></i>
                          <span className="fw-medium">Courses</span>
                        </div>
                      </button>
                    </>
                  )}
                </div>
              </div>

              {/* My Topics (only show in dashboard view) */}
              {currentView === 'dashboard' && (
                <div className="mb-4">
                  <div className="d-flex align-items-center mb-3">
                    <div className="rounded-circle d-flex align-items-center justify-content-center me-3" style={{
                      width: '40px', height: '40px',
                      background: 'linear-gradient(135deg, #667eea, #764ba2)'
                    }}>
                      <i className="fas fa-folder text-white"></i>
                    </div>
                    <div>
                      <h6 className="text-white mb-0 fw-bold">My Topics</h6>
                      <small className="text-white-50">Recent Courses</small>
                    </div>
                  </div>

                  {topics.length ? (
                    <div className="list-group list-group-flush">
                      {topics.map((t, idx) => (
                        <div
                          key={idx}
                          className="list-group-item bg-transparent text-white border-0 px-3 py-3 rounded-3 mb-2 position-relative overflow-hidden"
                          style={{ 
                            cursor: "pointer",
                            background: "rgba(255, 255, 255, 0.1)",
                            backdropFilter: "blur(10px)",
                            transition: "all 0.3s ease"
                          }}
                          onClick={() => {
                            setTopic(t);
                            generateCourse();
                          }}
                          onMouseEnter={(e) => {
                            e.currentTarget.style.background = "rgba(255, 255, 255, 0.2)";
                            e.currentTarget.style.transform = "translateX(8px)";
                          }}
                          onMouseLeave={(e) => {
                            e.currentTarget.style.background = "rgba(255, 255, 255, 0.1)";
                            e.currentTarget.style.transform = "translateX(0)";
                          }}
                        >
                          <div className="d-flex align-items-center">
                            <i className="fas fa-play-circle me-3 text-primary"></i>
                            <span className="fw-medium">{t}</span>
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-center py-3">
                      <i className="fas fa-inbox fa-2x text-white-50 mb-2"></i>
                      <p className="text-white-50 small mb-0">No topics yet</p>
                    </div>
                  )}
                </div>
              )}

             

              {/* Logout Button */}
              <div className="mt-auto">
                <button 
                  className="btn btn-outline-light w-100 rounded-pill"
                  onClick={handleLogout}
                  style={{ borderWidth: '2px' }}
                >
                  <i className="fas fa-sign-out-alt me-2"></i>Logout
                </button>
              </div>
            </>
          )}
        </div>
      </div>

      {/* Main content */}
      <div
        className="flex-grow-1"
        style={{
          marginLeft: sidebarCollapsed ? "80px" : "320px",
          transition: "margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1)"
        }}
      >
        <div className="container-fluid py-5">
          {/* Admin Views */}
          {currentView === 'admin-analytics' && user?.role === 'ADMIN' && <AdminAnalytics />}
          {currentView === 'admin-users' && user?.role === 'ADMIN' && <AdminUsers />}
          {currentView === 'admin-courses' && user?.role === 'ADMIN' && <AdminCourses />}

          {/* Dashboard View */}
          {currentView === 'dashboard' && (
            <>
              {/* Header */}
              <div className="text-center mb-5 floating">
                <div className="d-inline-flex align-items-center mb-4">
                  <div className="rounded-circle d-flex align-items-center justify-content-center me-4" style={{
                    width: '80px', height: '80px',
                    background: 'rgba(255, 255, 255, 0.2)',
                    backdropFilter: 'blur(20px)'
                  }}>
                    <i className="fas fa-robot fa-2x text-white"></i>
                  </div>
                  <div className="text-start">
                    <h1 className="text-white mb-2">AI Course Builder</h1>
                    <p className="text-white-75 fs-5 mb-0">
                      Welcome back, {user.name.split(' ')[0]}! 👋
                      {user.role === 'ADMIN' && (
                        <span className="badge bg-warning text-dark ms-2">ADMIN</span>
                      )}
                    </p>
                  </div>
                </div>
              </div>

              {/* Search Input */}
              <div className="row justify-content-center mb-5">
                <div className="col-lg-8">
                  <div className="glass-card p-4 shadow-glow">
                    <div className="input-group input-group-lg">
                      <span className="input-group-text border-0 bg-transparent">
                        <i className="fas fa-search text-white"></i>
                      </span>
                      <input
                        type="text"
                        className="form-control border-0 bg-transparent text-white"
                        placeholder="Enter a topic (e.g., Machine Learning, React Development, Python Basics)"
                        value={topic}
                        onChange={(e) => setTopic(e.target.value)}
                        onKeyPress={handleKeyPress}
                        disabled={loading}
                        style={{ fontSize: "1.1rem" }}
                      />
                      <button
                        className="btn btn-primary px-4 rounded-end-3"
                        onClick={generateCourse}
                        disabled={loading || !topic.trim()}
                        style={{
                          background: loading ? 'rgba(255,255,255,0.1)' : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                          border: 'none',
                          minWidth: '140px'
                        }}
                      >
                        {loading ? (
                          <>
                            <div className="spinner-border spinner-border-sm me-2"></div>
                            Generating...
                          </>
                        ) : (
                          <>
                            <i className="fas fa-magic me-2"></i>Generate
                          </>
                        )}
                      </button>
                    </div>
                    
                    {/* Progress Bar */}
                    {loading && (
                      <div className="mt-3">
                        <div className="d-flex justify-content-between align-items-center mb-2">
                          <small className="text-white-75">Generating your personalized course...</small>
                          <small className="text-white-75">{Math.round(progress)}%</small>
                        </div>
                        <div className="progress" style={{ height: '8px', background: 'rgba(255,255,255,0.1)' }}>
                          <div
                            className="progress-bar"
                            role="progressbar"
                            style={{
                              width: `${progress}%`,
                              background: 'linear-gradient(90deg, #667eea, #764ba2)',
                              transition: 'width 0.3s ease'
                            }}
                          ></div>
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              </div>

              {/* Error Display */}
              {error && (
                <div className="row justify-content-center mb-4">
                  <div className="col-lg-8">
                    <div className="alert alert-danger border-0 rounded-4" role="alert">
                      <div className="d-flex align-items-center">
                        <i className="fas fa-exclamation-circle me-3 fs-4"></i>
                        <div>
                          <strong>Error:</strong> {error}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* Course Content */}
              {course && (
                <div className="row justify-content-center">
                  <div className="col-lg-10">
                    <div className="glass-card p-5 shadow-glow">
                      {/* Course Header */}
                      <div className="text-center mb-5">
                        <div className="d-inline-flex align-items-center mb-3">
                          <div className="rounded-circle d-flex align-items-center justify-content-center me-3" style={{
                            width: '60px', height: '60px',
                            background: 'linear-gradient(135deg, #667eea, #764ba2)'
                          }}>
                            <i className="fas fa-graduation-cap text-white fa-lg"></i>
                          </div>
                          <div className="text-start">
                            <h2 className="text-white mb-1">{course.title}</h2>
                            <div className="d-flex align-items-center">
                              <span className="badge rounded-pill me-2" style={{ 
                                background: 'rgba(255,255,255,0.2)', 
                                color: 'white' 
                              }}>
                                {course.lessons?.length || 0} Lessons
                              </span>
                              <span className="badge rounded-pill" style={{ 
                                background: 'rgba(255,255,255,0.2)', 
                                color: 'white' 
                              }}>
                                AI Generated
                              </span>
                            </div>
                          </div>
                        </div>
                        {course.description && (
                          <p className="text-white-75 fs-6 mb-0 mx-auto" style={{ maxWidth: '600px' }}>
                            {course.description}
                          </p>
                        )}
                      </div>

                      {/* Lessons */}
                      {course.lessons?.length > 0 && (
                        <div className="lessons-container">
                          {course.lessons.map((lesson, lessonIndex) => (
                            <div key={lessonIndex} className="mb-5">
                              {/* Lesson Header */}
                              <div className="d-flex align-items-center mb-4">
                                <div className="rounded-circle d-flex align-items-center justify-content-center me-3" style={{
                                  width: '50px', height: '50px',
                                  background: 'linear-gradient(135deg, #667eea, #764ba2)',
                                  color: 'white'
                                }}>
                                  <span className="fw-bold">{lessonIndex + 1}</span>
                                </div>
                                <div>
                                  <h4 className="text-white mb-1">{lesson.title}</h4>
                                  <small className="text-white-50">Lesson {lessonIndex + 1}</small>
                                </div>
                              </div>

                              {/* Lesson Content */}
                              {lesson.description && (
                                <div className="glass-card p-4 mb-4">
                                  <div className="text-white" style={{ lineHeight: '1.8' }}>
                                    {lesson.description.split('\n').map((paragraph, pIndex) => (
                                      paragraph.trim() && (
                                        <p key={pIndex} className="mb-3">
                                          {paragraph}
                                        </p>
                                      )
                                    ))}
                                  </div>
                                </div>
                              )}

                              {/* Video */}
                              {lesson.videoUrl && (
                                <div className="glass-card p-4 mb-4">
                                  <div className="d-flex align-items-center mb-3">
                                    <i className="fas fa-play-circle text-primary me-2 fs-5"></i>
                                    <h6 className="text-white mb-0">Video Tutorial</h6>
                                  </div>
                                  <div className="ratio ratio-16x9 rounded-3 overflow-hidden">
                                    <iframe
                                      src={embedUrl(lesson.videoUrl)}
                                      title={`Video for ${lesson.title}`}
                                      allowFullScreen
                                      style={{ border: 'none' }}
                                    ></iframe>
                                  </div>
                                </div>
                              )}

                              {/* Quizzes */}
                              {lesson.quizzes?.length > 0 && (
                                <div className="quizzes-section">
                                  <div className="d-flex align-items-center mb-3">
                                    <i className="fas fa-brain text-warning me-2 fs-5"></i>
                                    <h5 className="text-white mb-0">Knowledge Check</h5>
                                  </div>
                                  {lesson.quizzes.map((quiz, quizIndex) => (
                                    <InteractiveQuiz
                                      key={quizIndex}
                                      quiz={quiz}
                                      quizIndex={quizIndex}
                                      lessonIndex={lessonIndex}
                                    />
                                  ))}
                                </div>
                              )}
                            </div>
                          ))}
                        </div>
                      )}

                      {/* Course Summary */}
                      {course.summary && (
                        <div className="glass-card p-4 mt-5">
                          <div className="d-flex align-items-center mb-3">
                            <i className="fas fa-lightbulb text-warning me-2 fs-5"></i>
                            <h5 className="text-white mb-0">Course Summary</h5>
                          </div>
                          <div className="text-white-75" style={{ lineHeight: '1.8' }}>
                            {course.summary.split('\n').map((paragraph, pIndex) => (
                              paragraph.trim() && (
                                <p key={pIndex} className="mb-3">
                                  {paragraph}
                                </p>
                              )
                            ))}
                          </div>
                        </div>
                      )}

                      {/* Action Buttons */}
                      <div className="text-center mt-5">
                        <button
                          className="btn btn-primary btn-lg px-5 py-3 rounded-pill me-3"
                          onClick={() => {
                            setTopic("");
                            setCourse(null);
                            setQuizAnswers({});
                            setQuizSubmitted({});
                            setQuizResults({});
                          }}
                          style={{
                            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                            border: 'none'
                          }}
                        >
                          <i className="fas fa-plus me-2"></i>Create New Course
                        </button>
                        <button
                          className="btn btn-outline-light btn-lg px-5 py-3 rounded-pill"
                          onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
                          style={{ borderWidth: '2px' }}
                        >
                          <i className="fas fa-arrow-up me-2"></i>Back to Top
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* Welcome Message when no course */}
              {!course && !loading && (
                <div className="row justify-content-center">
                  <div className="col-lg-8 text-center">
                    <div className="mb-5">
                      <div className="d-inline-flex align-items-center justify-content-center rounded-circle mb-4" style={{
                        width: '120px', height: '120px',
                        background: 'rgba(255, 255, 255, 0.1)',
                        backdropFilter: 'blur(20px)'
                      }}>
                        <i className="fas fa-rocket fa-3x text-white"></i>
                      </div>
                      <h3 className="text-white mb-3">Ready to Learn Something New?</h3>
                      <p className="text-white-75 fs-5 mx-auto" style={{ maxWidth: '500px' }}>
                        Enter any topic above and I'll create a personalized course with lessons, videos, and interactive quizzes tailored just for you!
                      </p>
                    </div>
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </div>

      {/* Custom Styles */}
      <style jsx>{`
        .animated-bg {
          background: linear-gradient(-45deg, #667eea, #764ba2, #667eea, #764ba2);
          background-size: 400% 400%;
          animation: gradientShift 15s ease infinite;
        }

        @keyframes gradientShift {
          0% { background-position: 0% 50%; }
          50% { background-position: 100% 50%; }
          100% { background-position: 0% 50%; }
        }

        .glass-card {
          background: rgba(255, 255, 255, 0.1);
          backdrop-filter: blur(20px);
          border: 1px solid rgba(255, 255, 255, 0.2);
          border-radius: 20px;
        }

        .shadow-glow {
          box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
        }

        .floating {
          animation: floating 3s ease-in-out infinite;
        }

        @keyframes floating {
          0%, 100% { transform: translateY(0px); }
          50% { transform: translateY(-10px); }
        }

        .text-white-75 {
          color: rgba(255, 255, 255, 0.75) !important;
        }

        .text-white-50 {
          color: rgba(255, 255, 255, 0.5) !important;
        }

        .custom-scrollbar::-webkit-scrollbar {
          width: 6px;
        }

        .custom-scrollbar::-webkit-scrollbar-track {
          background: rgba(255, 255, 255, 0.1);
          border-radius: 10px;
        }

        .custom-scrollbar::-webkit-scrollbar-thumb {
          background: rgba(255, 255, 255, 0.3);
          border-radius: 10px;
        }

        .custom-scrollbar::-webkit-scrollbar-thumb:hover {
          background: rgba(255, 255, 255, 0.5);
        }

        .sidebar {
          backdrop-filter: blur(20px);
          border-right: 1px solid rgba(255, 255, 255, 0.1);
        }

        .form-control:focus {
          background-color: transparent !important;
          border-color: rgba(255, 255, 255, 0.3) !important;
          box-shadow: 0 0 0 0.2rem rgba(255, 255, 255, 0.1) !important;
          color: white !important;
        }

        .form-control::placeholder {
          color: rgba(255, 255, 255, 0.6) !important;
        }

        .btn:hover {
          transform: translateY(-2px);
          transition: all 0.3s ease;
        }

        .lessons-container > div:not(:last-child)::after {
          content: '';
          display: block;
          width: 2px;
          height: 50px;
          background: linear-gradient(to bottom, transparent, rgba(255,255,255,0.2), transparent);
          margin: 30px auto;
        }
`}</style>
    </div>
  );
}

export default App;

       