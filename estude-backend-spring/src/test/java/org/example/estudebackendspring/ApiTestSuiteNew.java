package org.example.estudebackendspring;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.estudebackendspring.dto.*;
import org.example.estudebackendspring.enums.DifficultyLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Bộ Test Suite API mới cho EStude LMS
 * Thay thế các test case bị failed bằng test cases hoạt động
 * Dựa trên các controller thực tế trong hệ thống
 * 
 * @author QA Team
 * @version 2.0
 * @date 2025-11-29
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("EStude LMS - API Test Suite (New)")
public class ApiTestSuiteNew {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String studentToken;
    private String teacherToken;
    private String adminToken;
    
    // Test credentials
    private static final String ADMIN_USERNAME = "admin01";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String STUDENT_USERNAME = "student1";
    private static final String STUDENT_PASSWORD = "123456";
    private static final String TEACHER_USERNAME = "teacher01";
    private static final String TEACHER_PASSWORD = "teacher123";

    // ===============================================
    // HELPER METHODS
    // ===============================================

    @BeforeEach
    void setUp() throws Exception {
        // Login để lấy tokens trước mỗi test
        studentToken = loginAsStudent();
        teacherToken = loginAsTeacher();
        adminToken = loginAsAdmin();
    }

    private String loginAsStudent() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(STUDENT_USERNAME);
        request.setPassword(STUDENT_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/login-student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(response, LoginResponse.class);
        return loginResponse.getToken();
    }

    private String loginAsTeacher() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(TEACHER_USERNAME);
        request.setPassword(TEACHER_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/login-teacher")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(response, LoginResponse.class);
        return loginResponse.getToken();
    }

    private String loginAsAdmin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(ADMIN_USERNAME);
        request.setPassword(ADMIN_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/login-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(response, LoginResponse.class);
        return loginResponse.getToken();
    }

    // ===============================================
    // TEST CASES - AUTHENTICATION
    // ===============================================

    @Test
    @DisplayName("TC01 - Đăng nhập Admin thành công")
    void testAdminLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(ADMIN_USERNAME);
        request.setPassword(ADMIN_PASSWORD);

        mockMvc.perform(post("/api/auth/login-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user").exists());
    }

    @Test
    @DisplayName("TC02 - Đăng nhập Student thành công")
    void testStudentLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(STUDENT_USERNAME);
        request.setPassword(STUDENT_PASSWORD);

        mockMvc.perform(post("/api/auth/login-student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("TC03 - Đăng nhập Teacher thành công")
    void testTeacherLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(TEACHER_USERNAME);
        request.setPassword(TEACHER_PASSWORD);

        mockMvc.perform(post("/api/auth/login-teacher")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("TC04 - Đăng nhập thất bại với mật khẩu sai")
    void testLoginFailWithWrongPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(STUDENT_USERNAME);
        request.setPassword("wrong_password");

        mockMvc.perform(post("/api/auth/login-student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid password"));
    }

    @Test
    @DisplayName("TC05 - Đăng nhập thất bại với username không tồn tại")
    void testLoginFailWithInvalidUsername() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent_user");
        request.setPassword("any_password");

        mockMvc.perform(post("/api/auth/login-student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===============================================
    // TEST CASES - STUDENT ENDPOINTS
    // ===============================================

    @Test
    @DisplayName("TC06 - Lấy danh sách tất cả students")
    void testGetAllStudents() throws Exception {
        mockMvc.perform(get("/api/students")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("TC07 - Lấy thông tin student theo ID")
    void testGetStudentById() throws Exception {
        // Sử dụng studentId = 1 (giả sử có trong test data)
        mockMvc.perform(get("/api/students/1")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists());
    }

    @Test
    @DisplayName("TC08 - Lấy danh sách lớp học của student")
    void testGetStudentClasses() throws Exception {
        mockMvc.perform(get("/api/students/1/classes")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("TC09 - Lấy danh sách môn học của student")
    void testGetStudentSubjects() throws Exception {
        mockMvc.perform(get("/api/students/1/subjects")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("TC10 - Lấy danh sách enrollments của student")
    void testGetStudentEnrollments() throws Exception {
        mockMvc.perform(get("/api/students/1/enrollments")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ===============================================
    // TEST CASES - SUBJECT ENDPOINTS
    // ===============================================

    @Test
    @DisplayName("TC11 - Lấy danh sách tất cả môn học")
    void testGetAllSubjects() throws Exception {
        mockMvc.perform(get("/api/subjects")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("TC12 - Lấy thông tin môn học theo ID")
    void testGetSubjectById() throws Exception {
        mockMvc.perform(get("/api/subjects/1")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectId").exists());
    }

    @Test
    @DisplayName("TC13 - Tạo môn học mới")
    void testCreateSubject() throws Exception {
        CreateSubjectRequest request = new CreateSubjectRequest("Test Subject", "Test Description");

        mockMvc.perform(post("/api/subjects")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subjectId").exists())
                .andExpect(jsonPath("$.name").value("Test Subject"));
    }

    // ===============================================
    // TEST CASES - QUESTION BANK ENDPOINTS
    // ===============================================

    @Test
    @DisplayName("TC14 - Lấy tất cả câu hỏi trong question bank")
    void testGetAllQuestionBank() throws Exception {
        mockMvc.perform(get("/api/questions/bank")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("TC15 - Tạo câu hỏi mới trong question bank")
    void testCreateQuestionBank() throws Exception {
        // Tạo options
        List<QuestionBankRequest.QuestionOptionRequest> options = new ArrayList<>();
        options.add(new QuestionBankRequest.QuestionOptionRequest("3", false, 1));
        options.add(new QuestionBankRequest.QuestionOptionRequest("4", true, 2));
        options.add(new QuestionBankRequest.QuestionOptionRequest("5", false, 3));
        options.add(new QuestionBankRequest.QuestionOptionRequest("6", false, 4));
        
        QuestionBankRequest request = new QuestionBankRequest(
            "What is 2+2?",
            1.0f,
            "MULTIPLE_CHOICE",
            1L,
            "EASY",
            null,
            options
        );

        mockMvc.perform(post("/api/questions/bank")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("TC16 - Lấy câu hỏi theo topic")
    void testGetQuestionsByTopic() throws Exception {
        mockMvc.perform(get("/api/questions/bank/topic/1")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("TC17 - Lấy câu hỏi theo topic và difficulty")
    void testGetQuestionsByTopicAndDifficulty() throws Exception {
        mockMvc.perform(get("/api/questions/bank/topic/1")
                .param("difficulty", "EASY")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("TC18 - Đếm số câu hỏi trong topic")
    void testCountQuestionsByTopic() throws Exception {
        mockMvc.perform(get("/api/questions/bank/topic/1/count")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ===============================================
    // TEST CASES - PRACTICE TEST ENDPOINTS
    // ===============================================

    @Test
    @DisplayName("TC19 - Tạo practice test mới")
    void testCreatePracticeTest() throws Exception {
        List<Long> topicIds = new ArrayList<>();
        topicIds.add(1L);
        
        CreatePracticeTestRequest request = new CreatePracticeTestRequest(
            1L,  // subjectId
            1L,  // studentId
            topicIds,
            10,  // numQuestions
            "MEDIUM",
            30   // timeLimit
        );

        mockMvc.perform(post("/api/practice-tests/create")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testId").exists());
    }

    @Test
    @DisplayName("TC20 - Lấy danh sách practice tests của student")
    void testGetStudentPracticeTests() throws Exception {
        mockMvc.perform(get("/api/practice-tests/student/1")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("TC21 - Lấy practice tests chưa hoàn thành")
    void testGetIncompletePracticeTests() throws Exception {
        mockMvc.perform(get("/api/practice-tests/student/1")
                .param("completed", "false")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("TC22 - Lấy practice tests đã hoàn thành")
    void testGetCompletedPracticeTests() throws Exception {
        mockMvc.perform(get("/api/practice-tests/student/1")
                .param("completed", "true")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("TC23 - Lấy chi tiết practice test")
    void testGetPracticeTestDetails() throws Exception {
        // Giả sử có test với ID = 1
        mockMvc.perform(get("/api/practice-tests/1")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testId").exists());
    }

    @Test
    @DisplayName("TC24 - Submit practice test")
    void testSubmitPracticeTest() throws Exception {
        // Tạo danh sách answers (giả sử có 5 câu)
        List<AnswerRequest> answers = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            AnswerRequest answer = new AnswerRequest(
                (long) i,  // questionId
                (long) i,  // chosenOptionId
                null,      // textAnswer
                null       // fileUrl
            );
            answers.add(answer);
        }
        
        SubmissionRequest request = new SubmissionRequest(
            1L,  // assignmentId
            1L,  // studentId
            answers,
            "Test submission"
        );

        mockMvc.perform(post("/api/practice-tests/1/submit")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submissionId").exists());
    }

    // ===============================================
    // TEST CASES - AUTHORIZATION TESTS
    // ===============================================

    @Test
    @DisplayName("TC25 - Truy cập không có token - 401/403")
    void testAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TC26 - Truy cập với token không hợp lệ")
    void testAccessWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/students")
                .header("Authorization", "Bearer invalid_token_here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TC27 - Student không thể truy cập admin endpoint")
    void testStudentCannotAccessAdminEndpoint() throws Exception {
        CreateSubjectRequest request = new CreateSubjectRequest("Unauthorized Subject", "Test");

        mockMvc.perform(post("/api/subjects")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ===============================================
    // TEST CASES - DATA VALIDATION
    // ===============================================

    @Test
    @DisplayName("TC28 - Tạo subject với dữ liệu thiếu - validation error")
    void testCreateSubjectWithMissingData() throws Exception {
        CreateSubjectRequest request = new CreateSubjectRequest("", "Test");
        // Empty name - should fail validation

        mockMvc.perform(post("/api/subjects")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC29 - Lấy student không tồn tại - 404")
    void testGetNonExistentStudent() throws Exception {
        mockMvc.perform(get("/api/students/99999")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("TC30 - Lấy subject không tồn tại - 404")
    void testGetNonExistentSubject() throws Exception {
        mockMvc.perform(get("/api/subjects/99999")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
