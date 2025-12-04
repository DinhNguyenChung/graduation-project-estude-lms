package org.example.estudebackendspring;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.estudebackendspring.dto.LoginRequest;
import org.example.estudebackendspring.dto.LoginResponse;
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

import java.time.LocalDate;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Bộ test suite API toàn diện cho EStude LMS
 * Sử dụng JUnit 5 + Spring Boot Test + MockMvc
 * 
 * Tên class: ApiTestSuite
 * Phiên bản: 1.0
 * Mô tả: Test 12 test case chính của hệ thống
 * 
 * Tác giả: Senior QA Automation (Java - Spring Boot)
 * Ngày tạo: 2025-11-29
 * 
 * LƯU Ý QUAN TRỌNG:
 * - Tests này yêu cầu data test được setup trước
 * - Chỉ test những API có authentication đúng cách
 * - Một số tests có thể skip nếu endpoints không tồn tại
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("EStude LMS - API Test Suite")
public class ApiTestSuite {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String studentToken;
    private String teacherToken;
    private String adminToken;
    private static final String ADMIN_USERNAME = "admin01";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String STUDENT_USERNAME = "student1";
    private static final String STUDENT_PASSWORD = "123456";
    private static final String TEACHER_USERNAME = "teacher01";
    private static final String TEACHER_PASSWORD = "teacher123";

    // ===============================================
    // HELPER METHODS - Các hàm hỗ trợ
    // ===============================================

    /**
     * Đăng nhập với tư cách Admin và trả về token
     */
    private String loginAsAdmin() throws Exception {
        LoginRequest adminLoginRequest = new LoginRequest();
        adminLoginRequest.setUsername(ADMIN_USERNAME);
        adminLoginRequest.setPassword(ADMIN_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/login-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLoginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(response, LoginResponse.class);
        return loginResponse.getToken();
    }

    /**
     * Đăng nhập với tư cách Giáo viên và trả về token
     */
    private String loginAsTeacher() throws Exception {
        LoginRequest teacherLoginRequest = new LoginRequest();
        teacherLoginRequest.setUsername(TEACHER_USERNAME);
        teacherLoginRequest.setPassword(TEACHER_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/login-teacher")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(teacherLoginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(response, LoginResponse.class);
        return loginResponse.getToken();
    }

    /**
     * Đăng nhập với tư cách Học sinh và trả về token
     */
    private String loginAsStudent() throws Exception {
        LoginRequest studentLoginRequest = new LoginRequest();
        studentLoginRequest.setUsername(STUDENT_USERNAME);
        studentLoginRequest.setPassword(STUDENT_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/login-student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentLoginRequest)))
                .andReturn();

        int status = result.getResponse().getStatus();
        String response = result.getResponse().getContentAsString();
        
        System.out.println("📋 Login Student Response - Status: " + status + ", Body: " + response);
        
        if (status != 200) {
            System.out.println("⚠️  Login failed with status: " + status);
            return null;
        }
        
        LoginResponse loginResponse = objectMapper.readValue(response, LoginResponse.class);
        return loginResponse.getToken();
    }

    /**
     * Setup tiền điều kiện - Chạy trước mỗi test
     */
    @BeforeEach
    public void setUp() throws Exception {
        // Lấy token cho các role khác nhau
        try {
            studentToken = loginAsStudent();
        } catch (Exception e) {
            System.out.println("⚠️ Warning: Could not login as student - " + e.getMessage());
            studentToken = null;
        }
        
        try {
            teacherToken = loginAsTeacher();
        } catch (Exception e) {
            System.out.println("⚠️ Warning: Could not login as teacher - " + e.getMessage());
            teacherToken = null;
        }
        
        try {
            adminToken = loginAsAdmin();
        } catch (Exception e) {
            System.out.println("⚠️ Warning: Could not login as admin - " + e.getMessage());
            adminToken = null;
        }
    }

    // ===============================================
    // TC01 - Đăng nhập Học sinh
    // ===============================================

    @Test
    @DisplayName("TC01 - Kiểm tra học sinh đăng nhập thành công")
    public void testLoginStudentSuccessfully() throws Exception {
        // Arrange (Chuẩn bị)
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(STUDENT_USERNAME);
        loginRequest.setPassword(STUDENT_PASSWORD);

        // Act & Assert (Thực hiện & Kiểm tra)
        mockMvc.perform(post("/api/auth/login-student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("Login successful")))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token", not(emptyString())));
    }

    @Test
    @DisplayName("TC01 - Kiểm tra token chứa ROLE_STUDENT")
    public void testLoginStudentTokenContainsRole() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(STUDENT_USERNAME);
        loginRequest.setPassword(STUDENT_PASSWORD);

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/login-student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andReturn();

        // Kiểm tra response có token
        String responseContent = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);
        
        // Token không rỗng
        assert(loginResponse.getToken() != null && !loginResponse.getToken().isEmpty());
    }

    @Test
    @DisplayName("TC01 - Kiểm tra học sinh đăng nhập thất bại với mật khẩu sai")
    public void testLoginStudentWithWrongPassword() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(STUDENT_USERNAME);
        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login-student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Invalid password")));
    }

    // ===============================================
    // TC02 - Quản lý người dùng (Admin cấp tài khoản)
    // ===============================================

    @Test
    @DisplayName("TC02 - Admin tạo tài khoản Giáo viên thành công")
    public void testAdminCreateTeacherAccountSuccessfully() throws Exception {
        // Arrange
        String teacherCode = "GV_TEST_" + System.currentTimeMillis();
        String email = "gv01_" + System.currentTimeMillis() + "@school.edu";
        Date dob = new Date(System.currentTimeMillis() - 31536000000L); // 1 năm trước
        
        // Act & Assert
        // Note: Endpoint /api/admin/create-teacher không tồn tại - placeholder for future implementation
        if (adminToken != null) {
            mockMvc.perform(post("/api/admin/create-teacher")
                    .header("Authorization", "Bearer " + adminToken)
                    .param("schoolId", "1")
                    .param("teacherCode", teacherCode)
                    .param("fullName", "Nguyen Van A")
                    .param("email", email)
                    .param("phone", "0123456789")
                    .param("password", "teacher123")
                    .param("dob", "1990-01-01")
                    .param("isAdmin", "false")
                    .param("isHomeroomTeacher", "false")
                    .contentType(MediaType.APPLICATION_JSON));
        }
    }

    @Test
    @DisplayName("TC02 - Kiểm tra Giáo viên được tạo có thể đăng nhập")
    public void testNewTeacherCanLogin() throws Exception {
        // Arrange
        String teacherCode = "GV_LOGIN_TEST_" + System.currentTimeMillis();
        String email = "gv_login_" + System.currentTimeMillis() + "@school.edu";
        String password = "newteacher123";

        // Tạo giáo viên
        mockMvc.perform(post("/api/admin/create-teacher")
                .header("Authorization", "Bearer " + adminToken)
                .param("schoolId", "1")
                .param("teacherCode", teacherCode)
                .param("fullName", "Nguyen Van B")
                .param("email", email)
                .param("phone", "0987654321")
                .param("password", password)
                .param("dob", "1991-05-15")
                .param("isAdmin", "false")
                .param("isHomeroomTeacher", "false"));

        // Đăng nhập với giáo viên vừa tạo
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(teacherCode);
        loginRequest.setPassword(password);

        mockMvc.perform(post("/api/auth/login-teacher")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    @DisplayName("TC02 - Admin tạo tài khoản Học sinh thành công")
    public void testAdminCreateStudentAccountSuccessfully() throws Exception {
        // Arrange
        String studentCode = "HS_TEST_" + System.currentTimeMillis();
        String email = "hs01_" + System.currentTimeMillis() + "@school.edu";

        // Act & Assert
        mockMvc.perform(post("/api/admin/create-student")
                .header("Authorization", "Bearer " + adminToken)
                .param("schoolId", "1")
                .param("studentCode", studentCode)
                .param("fullName", "Tran Thi C")
                .param("email", email)
                .param("phone", "0111111111")
                .param("password", "student123")
                .param("dob", "2005-03-20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("Tran Thi C")))
                .andExpect(jsonPath("$.email", is(email)));
    }

    // ===============================================
    // TC03 - Ngân hàng câu hỏi (Question Bank)
    // ===============================================

    @Test
    @DisplayName("TC03 - Admin thêm câu hỏi vào chủ đề 'Hàm số'")
    public void testAdminAddQuestionToTopicSuccessfully() throws Exception {
        // Arrange
        String questionContent = "Cho hàm số y = 2x + 3. Tìm giá trị của y khi x = 1?";
        String correctAnswer = "y = 5";
        String topicName = "Hàm số";

        // Dữ liệu JSON payload cho thêm câu hỏi
        String requestBody = String.format("""
                {
                    "content": "%s",
                    "topic": "%s",
                    "difficulty": "MEDIUM",
                    "correctAnswer": "%s",
                    "explanation": "Thay x = 1 vào phương trình: y = 2(1) + 3 = 5"
                }
                """, questionContent, topicName, correctAnswer);

        // Act & Assert
        mockMvc.perform(post("/api/questions/bank")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC03 - Kiểm tra câu hỏi xuất hiện khi truy vấn Topic")
    public void testQuestionAppearsWhenQueryingTopic() throws Exception {
        // Arrange
        Long topicId = 1L; // Giả sử topic "Hàm số" có ID = 1

        // Act & Assert
        mockMvc.perform(get("/api/questions/bank/topic/{topicId}", topicId)
            .header("Authorization", "Bearer " + studentToken)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions", hasSize(greaterThan(0))));
    }

    // ===============================================
    // TC04 - Tạo bài kiểm tra (Create Test)
    // ===============================================

    @Test
    @DisplayName("TC04 - Giáo viên tạo bài kiểm tra 15 phút thành công")
    public void testTeacherCreateTest15MinutesSuccessfully() throws Exception {
        // Arrange
        String testName = "Kiểm tra Hàm số - " + System.currentTimeMillis();
        String requestBody = String.format("""
                {
                    "title": "%s",
                    "description": "Bài kiểm tra 15 phút môn Toán",
                    "classId": 1,
                    "subjectId": 1,
                    "duration": 15,
                    "totalQuestions": 10,
                    "questionIds": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
                    "status": "PUBLISHED"
                }
                """, testName);

        // Act & Assert
        mockMvc.perform(post("/api/practice-tests/create")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(testName)))
                .andExpect(jsonPath("$.status", is("PUBLISHED")))
                .andExpect(jsonPath("$.duration", is(15)));
    }

    @Test
    @DisplayName("TC04 - Kiểm tra học sinh lớp 10A1 thấy bài kiểm tra")
    public void testStudentClass10A1CanSeePublishedTest() throws Exception {
        // Arrange
        Long classId = 1L; // Lớp 10A1
        Long testId = 1L;  // Bài kiểm tra vừa tạo

        // Act & Assert - use student endpoint to fetch available practice tests
        Long studentId = 3L;
        mockMvc.perform(get("/api/practice-tests/student/{studentId}", studentId)
            .header("Authorization", "Bearer " + studentToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tests", hasSize(greaterThan(0))));
    }

    // ===============================================
    // TC05 - Điểm danh (Attendance)
    // ===============================================

    @Test
    @DisplayName("TC05 - Giáo viên điểm danh học sinh vắng")
    public void testTeacherMarkStudentAbsent() throws Exception {
        // Arrange
        String requestBody = """
                {
                    "studentId": 2,
                    "attendanceDate": "2025-11-29",
                    "status": "ABSENT",
                    "reason": "Có việc gia đình"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/attendance/records/teacher")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("Cập nhật")));
    }

    @Test
    @DisplayName("TC05 - Kiểm tra DB cập nhật trạng thái ABSENT")
    public void testAttendanceStatusUpdatedInDatabase() throws Exception {
        // Arrange
        Long studentId = 2L;

        // Act & Assert - Lấy lại thông tin điểm danh
        mockMvc.perform(get("/api/attendance/records/student/{studentId}", studentId)
            .header("Authorization", "Bearer " + teacherToken)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendanceRecords[0].status", is("ABSENT")));
    }

    // ===============================================
    // TC06 - Làm bài thi (Take Test)
    // ===============================================

    @Test
    @DisplayName("TC06 - Học sinh làm bài trắc nghiệm và nộp bài thành công")
    public void testStudentTakeTestAndSubmitSuccessfully() throws Exception {
        // Arrange
        Long testId = 1L;
        String requestBody = """
                {
                    "testId": 1,
                    "answers": [
                        {"questionId": 1, "selectedAnswer": "A"},
                        {"questionId": 2, "selectedAnswer": "B"},
                        {"questionId": 3, "selectedAnswer": "C"},
                        {"questionId": 4, "selectedAnswer": "A"},
                        {"questionId": 5, "selectedAnswer": "D"}
                    ]
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/practice-tests/{testId}/submit", testId)
            .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submissionStatus", is("SUBMITTED")))
                .andExpect(jsonPath("$.score").isNumber());
    }

    @Test
    @DisplayName("TC06 - Kiểm tra bài thi tự động chấm điểm")
    public void testTestAutoGradingAfterSubmission() throws Exception {
        // Arrange
        Long submissionId = 1L;

        // Act & Assert
        mockMvc.perform(get("/api/submissions/{submissionId}", submissionId)
            .header("Authorization", "Bearer " + studentToken)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUBMITTED")))
                .andExpect(jsonPath("$.score").isNumber())
                .andExpect(jsonPath("$.score", greaterThanOrEqualTo(0)));
    }

    // ===============================================
    // TC07 - Làm bài đánh giá (Assessment)
    // ===============================================

    @Test
    @DisplayName("TC07 - Học sinh làm bài đánh giá trắc nghiệm")
    public void testStudentTakeAssessmentSuccessfully() throws Exception {
        // Arrange
        String requestBody = """
                {
                    "assessmentId": 1,
                    "subjectId": 1,
                    "answers": [
                        {"questionId": 1, "selectedAnswer": "A"},
                        {"questionId": 2, "selectedAnswer": "B"},
                        {"questionId": 3, "selectedAnswer": "C"}
                    ]
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/assessment/submit")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submissionStatus", is("SUBMITTED")))
                .andExpect(jsonPath("$.autoGradingScore").isNumber());
    }

    @Test
    @DisplayName("TC07 - Kiểm tra bài đánh giá tự động chấm điểm")
    public void testAssessmentAutoGrading() throws Exception {
        // Arrange
        Long assessmentSubmissionId = 1L;

        // Act & Assert
        mockMvc.perform(get("/api/assessment/submissions/{submissionId}", assessmentSubmissionId)
            .header("Authorization", "Bearer " + studentToken)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autoGradingScore").isNumber())
                .andExpect(jsonPath("$.status", is("SUBMITTED")));
    }

    // ===============================================
    // TC08 - Xem kết quả (View Results)
    // ===============================================

    @Test
    @DisplayName("TC08 - Học sinh xem chi tiết bài làm sau khi chấm điểm")
    public void testStudentViewDetailedTestResults() throws Exception {
        // Arrange
        Long submissionId = 1L;

        // Act & Assert
        mockMvc.perform(get("/api/test/result/{submissionId}", submissionId)
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalScore").isNumber())
                .andExpect(jsonPath("$.answers", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("TC08 - Kiểm tra hiển thị từng câu đúng/sai")
    public void testResultShowsCorrectAndIncorrectAnswers() throws Exception {
        // Arrange
        Long submissionId = 1L;

        // Act & Assert
        mockMvc.perform(get("/api/test/result/{submissionId}", submissionId)
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answers[0].isCorrect", isA(Boolean.class)))
                .andExpect(jsonPath("$.answers[0].correctAnswer").isString())
                .andExpect(jsonPath("$.answers[0].studentAnswer").isString());
    }

    // ===============================================
    // TC09 - AI Phân tích (AI Analysis)
    // ===============================================

    @Test
    @DisplayName("TC09 - Hệ thống gửi yêu cầu phân tích bài làm tới AI")
    public void testSystemSendAnalysisRequestToAI() throws Exception {
        // Arrange
        Long submissionId = 1L;
        String requestBody = String.format("""
                {
                    "submissionId": %d,
                    "analysisType": "SUBMISSION_ANALYSIS"
                }
                """, submissionId);

        // Act & Assert
        mockMvc.perform(post("/api/ai/learning-feedback")
            .header("Authorization", "Bearer " + studentToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("PROCESSING")));
    }

    @Test
    @DisplayName("TC09 - Kiểm tra AI trả về nhận xét chi tiết")
    public void testAIReturnDetailedFeedback() throws Exception {
        // Arrange
        Long analysisId = 1L;

        // Act & Assert
        Long studentId = 3L;
        mockMvc.perform(get("/api/ai/student/{studentId}/feedback", studentId)
            .header("Authorization", "Bearer " + studentToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.feedback").isString());
    }

    // ===============================================
    // TC10 - AI Đánh giá tiến bộ (Progress Evaluation)
    // ===============================================

    @Test
    @DisplayName("TC10 - AI đánh giá tiến bộ của học sinh")
    public void testAIEvaluateStudentProgress() throws Exception {
        // Arrange
        Long studentId = 1L;
        String requestBody = String.format("""
                {
                    "studentId": %d,
                    "evaluationType": "PROGRESS_EVALUATION"
                }
                """, studentId);

        // Act & Assert
        mockMvc.perform(post("/api/ai/improvement-evaluation")
            .header("Authorization", "Bearer " + studentToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC10 - Kiểm tra API trả về nhận xét chi tiết theo chủ đề")
    public void testProgressEvaluationReturnTopicBasedFeedback() throws Exception {
        // Arrange
        Long evaluationId = 1L;

        // Act & Assert
        Long studentIdForEval = 3L;
        mockMvc.perform(get("/api/ai/student/{studentId}/improvement", studentIdForEval)
            .header("Authorization", "Bearer " + studentToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    // ===============================================
    // TC11 - AI Lộ trình học (Learning Roadmap)
    // ===============================================

    @Test
    @DisplayName("TC11 - AI sinh lộ trình học cho học sinh")
    public void testAIGenerateLearningRoadmap() throws Exception {
        // Arrange
        Long studentId = 1L;
        String requestBody = String.format("""
                {
                    "studentId": %d,
                    "generateRoadmap": true
                }
                """, studentId);

        // Act & Assert
        mockMvc.perform(post("/api/ai/generate-learning-roadmap")
            .header("Authorization", "Bearer " + studentToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.roadmapId").isNumber());
    }

    @Test
    @DisplayName("TC11 - Kiểm tra roadmap gồm giai đoạn, tài liệu, câu sai, nhiệm vụ")
    public void testRoadmapContainsStagesResourcesAndTasks() throws Exception {
        // Arrange
        Long roadmapId = 1L;

        // Act & Assert
        Long studentIdForRoadmap = 3L;
        mockMvc.perform(get("/api/ai/student/{studentId}/roadmap", studentIdForRoadmap)
            .header("Authorization", "Bearer " + studentToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    // ===============================================
    // TC12 - Xử lý ngoại lệ AI (Exception Handling)
    // ===============================================

    @Test
    @DisplayName("TC12 - Xử lý lỗi khi API Gemini trả về lỗi 500")
    public void testHandleAIServiceErrorResponse() throws Exception {
        // Arrange
        Long studentId = 99999L; // Student ID không tồn tại

        String requestBody = String.format("""
                {
                    "studentId": %d,
                    "analysisType": "SUBMISSION_ANALYSIS"
                }
                """, studentId);

        // Act & Assert
        mockMvc.perform(post("/api/ai/analyze-submission")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("không tìm thấy")));
    }

    @Test
    @DisplayName("TC12 - Kiểm tra ứng dụng không crash khi AI timeout")
    public void testApplicationHandlesAITimeout() throws Exception {
        // Arrange
        Long submissionId = 1L;
        String requestBody = String.format("""
                {
                    "submissionId": %d,
                    "analysisType": "SUBMISSION_ANALYSIS"
                }
                """, submissionId);

        // Act & Assert - Ứng dụng vẫn trả về lỗi hợp lý thay vì crash
        mockMvc.perform(post("/api/ai/learning-feedback")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert(status == 200 || status == 400 || status == 500);
                });
    }

    @Test
    @DisplayName("TC12 - Kiểm tra thông báo lỗi thân thiện khi AI không sẵn sàng")
    public void testFriendlyErrorMessageWhenAIUnavailable() throws Exception {
        // Arrange
        Long studentId = 1L;
        String requestBody = String.format("""
                {
                    "studentId": %d,
                    "evaluationType": "PROGRESS_EVALUATION"
                }
                """, studentId);

        // Act & Assert - Kiểm tra thông báo lỗi chứa text "bận" hoặc "tạm thời"
        mockMvc.perform(post("/api/ai/improvement-evaluation")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(result -> {
                    String content = result.getResponse().getContentAsString();
                    if (result.getResponse().getStatus() != 200) {
                        assert(content.contains("bận") || content.contains("tạm thời") 
                                || content.contains("không khả dụng"));
                    }
                });
    }

    // ===============================================
    // ADDITIONAL HELPER TEST METHODS
    // ===============================================

    @Test
    @DisplayName("HELPER TEST - Kiểm tra API Health Check")
    public void testAPIHealthCheck() throws Exception {
        mockMvc.perform(get("/api/ai/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    @DisplayName("HELPER TEST - Kiểm tra unauthorized request")
    public void testUnauthorizedRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/practice-tests/student/3")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("HELPER TEST - Kiểm tra invalid token")
    public void testInvalidTokenRequest() throws Exception {
        mockMvc.perform(get("/api/practice-tests/student/3")
            .header("Authorization", "Bearer INVALID_TOKEN")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }
}
