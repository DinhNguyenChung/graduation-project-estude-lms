package org.example.estudebackendspring;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.estudebackendspring.util.TestUtilHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base Test Class - Template để extend cho các test class khác
 * Cung cấp các common setup và helper methods
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseApiTest {
    
    @Autowired
    protected MockMvc mockMvc;
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    @Autowired
    protected TestUtilHelper testUtilHelper;
    
    // Common test data constants
    protected static final String TEST_ADMIN_USERNAME = "admin01";
    protected static final String TEST_ADMIN_PASSWORD = "admin123";
    protected static final String TEST_TEACHER_USERNAME = "teacher01";
    protected static final String TEST_TEACHER_PASSWORD = "teacher123";
    protected static final String TEST_STUDENT_USERNAME = "student1";
    protected static final String TEST_STUDENT_PASSWORD = "123456";
    
    protected static final Long TEST_SCHOOL_ID = 1L;
    protected static final Long TEST_CLASS_ID = 1L;
    protected static final Long TEST_SUBJECT_ID = 1L;
    
    /**
     * Cách sử dụng:
     * 
     * @SpringBootTest
     * @AutoConfigureMockMvc
     * public class StudentPortalTest extends BaseApiTest {
     *     
     *     @Test
     *     public void testCustomFeature() throws Exception {
     *         String requestBody = testUtilHelper.toJson(someObject);
     *         
     *         mockMvc.perform(post("/api/endpoint")
     *                 .contentType(MediaType.APPLICATION_JSON)
     *                 .content(requestBody))
     *             .andExpect(status().isOk());
     *     }
     * }
     */
}
