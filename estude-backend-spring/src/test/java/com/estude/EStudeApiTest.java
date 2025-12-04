package com.estude;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EStudeApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper: login and return token string
    private String loginAndGetToken(String username, String password) throws Exception {
        String payload = objectMapper.createObjectNode()
                .put("username", username)
                .put("password", password)
                .toString();

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }

    @Test
    @DisplayName("TC_01 - Đăng nhập Học sinh thành công")
    public void TC_01_loginStudent() throws Exception {
        String payload = objectMapper.createObjectNode()
                .put("username", "student1")
                .put("password", "123456")
                .toString();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_STUDENT"));
    }

    @Test
    @DisplayName("TC_02 - Admin cấp tài khoản mới cho Giáo viên")
    public void TC_02_adminCreateTeacherAccount() throws Exception {
        String adminToken = loginAndGetToken("admin", "admin123");

        String payload = objectMapper.createObjectNode()
                .put("role", "TEACHER")
                .put("name", "Nguyen Van A")
                .put("email", "gv01@school.edu")
                .put("username", "gv01")
                .put("password", "123456")
                .toString();

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("gv01@school.edu"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_TEACHER"));

        // Login with new account to verify can log in
        String loginPayload = objectMapper.createObjectNode()
                .put("username", "gv01")
                .put("password", "123456")
                .toString();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_TEACHER"));
    }

    @Test
    @DisplayName("TC_03 - Admin thêm câu hỏi trắc nghiệm")
    public void TC_03_adminAddQuestion() throws Exception {
        String adminToken = loginAndGetToken("admin", "admin123");

        String payload = objectMapper.createObjectNode()
                .put("topic", "Hàm số")
                .put("content", "Cho hàm số f(x) = ...; hỏi ...")
                .put("type", "MULTIPLE_CHOICE")
                .putPOJO("options", objectMapper.createArrayNode()
                        .add(objectMapper.createObjectNode().put("key", "A").put("text", "..."))
                        .add(objectMapper.createObjectNode().put("key", "B").put("text", "...")))
                .put("answer", "A")
                .toString();

        mockMvc.perform(post("/api/questions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.topic").value("Hàm số"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("TC_04 - Giáo viên tạo và xuất bản bài kiểm tra 15 phút")
    public void TC_04_teacherCreateAndPublishTest() throws Exception {
        String teacherToken = loginAndGetToken("teacher1", "123456");

        String payload = objectMapper.createObjectNode()
                .put("title", "Test 15p - Toán")
                .put("durationMinutes", 15)
                .put("classCode", "10A1")
                .put("published", true)
                .toString();

        String response = mockMvc.perform(post("/api/tests")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test 15p - Toán"))
                .andExpect(jsonPath("$.published").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long testId = objectMapper.readTree(response).get("id").asLong();

        // Student should see published test in its class feed
        String studentToken = loginAndGetToken("student1", "123456");

        mockMvc.perform(get("/api/classes/10A1/tests")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + testId + ")].published").value(true));
    }

    @Test
    @DisplayName("TC_05 - Giáo viên điểm danh học sinh vắng mặt")
    public void TC_05_teacherAttendanceMarkAbsent() throws Exception {
        String teacherToken = loginAndGetToken("teacher1", "123456");

        String payload = objectMapper.createObjectNode()
                .put("classCode", "10A1")
                .put("sessionId", "session-001")
                .put("studentUsername", "HocSinhB")
                .put("status", "ABSENT")
                .toString();

        mockMvc.perform(post("/api/attendance")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentUsername").value("HocSinhB"))
                .andExpect(jsonPath("$.status").value("ABSENT"));
    }

    @Test
    @DisplayName("TC_06 - Học sinh làm và nộp bài trắc nghiệm")
    public void TC_06_studentSubmitQuiz() throws Exception {
        String studentToken = loginAndGetToken("student1", "123456");

        String payload = objectMapper.createObjectNode()
                .put("testId", 1001)
                .putPOJO("answers", objectMapper.createArrayNode()
                        .add(objectMapper.createObjectNode().put("questionId", 501).put("answer", "A"))
                        .add(objectMapper.createObjectNode().put("questionId", 502).put("answer", "B")))
                .toString();

        mockMvc.perform(post("/api/submissions")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.score").isNumber());
    }

    @Test
    @DisplayName("TC_07 - Học sinh xem chi tiết bài làm đã chấm")
    public void TC_07_studentViewResultDetail() throws Exception {
        String studentToken = loginAndGetToken("student1", "123456");

        // giả sử submissionId = 2001 đã được chấm
        mockMvc.perform(get("/api/results/2001")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalScore").isNumber())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("TC_08 - AI phân tích điểm yếu trả feedback")
    public void TC_08_aiAnalyzeWeakness() throws Exception {
        String studentToken = loginAndGetToken("student1", "123456");

        String payload = objectMapper.createObjectNode()
                .put("submissionId", 2001)
                .toString();

        mockMvc.perform(post("/api/ai/analyze")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedback").isArray())
                .andExpect(jsonPath("$.feedback[0].text").isString());
    }

    @Test
    @DisplayName("TC_09 - AI sinh lộ trình học cá nhân")
    public void TC_09_aiGenerateRoadmap() throws Exception {
        String studentToken = loginAndGetToken("student1", "123456");

        String payload = objectMapper.createObjectNode()
                .put("target", "improve_math")
                .put("averageScore", 4.5)
                .toString();

        mockMvc.perform(post("/api/ai/roadmap")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.steps").isArray())
                .andExpect(jsonPath("$.steps[0].title").isString());
    }

    @Test
    @DisplayName("TC_10 - AI dự báo điểm cuối kỳ")
    public void TC_10_aiPredictFinalScore() throws Exception {
        String studentToken = loginAndGetToken("student1", "123456");

        mockMvc.perform(get("/api/ai/predict?studentUsername=student1")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prediction").isNumber())
                .andExpect(jsonPath("$.chartData").isMap());
    }

    @Test
    @DisplayName("TC_11 - Xử lý ngoại lệ AI khi API lỗi")
    public void TC_11_aiErrorHandling() throws Exception {
        String adminToken = loginAndGetToken("admin", "admin123");

        // Gọi endpoint AI với flag mô phỏng lỗi (server-side should handle and return friendly message)
        mockMvc.perform(post("/api/ai/analyze?mockError=true")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.message").exists());
    }
}
