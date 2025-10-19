# Learning Loop AI Integration - Backend Spring Boot

## 📋 Tổng quan

Backend Spring Boot đã được cập nhật để tích hợp với Learning Loop AI với 4 tầng xử lý:

1. **Layer 1**: AI Feedback & Topic Detection
2. **Layer 2**: Personalized Recommendation  
3. **Layer 3**: Practice Quiz Generation
4. **Layer 4**: Improvement Evaluation

## 🔄 Luồng hoạt động

```
🧑‍🎓 Học sinh làm bài test
       ↓
🤖 Layer 1: POST /api/ai/learning-feedback (Phân tích chi tiết)
       ↓
💡 Layer 2: POST /api/ai/learning-recommendation (Gợi ý cá nhân hóa)
       ↓
📝 Layer 3: POST /api/ai/generate-practice-quiz (Sinh bài luyện tập)
       ↓
🧮 Học sinh làm bài luyện
       ↓
📈 Layer 4: POST /api/ai/improvement-evaluation (Đánh giá tiến bộ)
```

## 🏗️ Cấu trúc code

### DTOs (Data Transfer Objects)

**Package**: `org.example.estudebackendspring.dto.learning`

#### Layer 1 - Feedback DTOs:
- `QuestionDTO` - Câu hỏi với đáp án đúng và đáp án học sinh
- `FeedbackRequest` - Request gửi đến AI
- `FeedbackResponse` - Response từ AI
- `QuestionFeedbackDTO` - Phản hồi chi tiết cho từng câu
- `FeedbackSummaryDTO` - Tổng kết kết quả
- `FeedbackDataDTO` - Dữ liệu feedback đầy đủ

#### Layer 2 - Recommendation DTOs:
- `RecommendationRequest` - Request với feedback data
- `RecommendationResponse` - Response với gợi ý
- `WeakTopicDTO` - Topic yếu cần cải thiện
- `TopicRecommendationDTO` - Gợi ý chi tiết cho topic
- `RecommendationDataDTO` - Dữ liệu recommendation đầy đủ

#### Layer 3 - Practice Quiz DTOs:
- `PracticeQuizRequest` - Request sinh câu hỏi
- `PracticeQuizResponse` - Response với câu hỏi
- `PracticeQuestionDTO` - Câu hỏi luyện tập
- `PracticeQuizDataDTO` - Dữ liệu quiz đầy đủ

#### Layer 4 - Improvement DTOs:
- `ImprovementRequest` - Request đánh giá tiến bộ
- `ImprovementResponse` - Response đánh giá
- `TopicResultDTO` - Kết quả theo topic
- `TopicImprovementDTO` - Tiến bộ theo topic
- `OverallImprovementDTO` - Tổng quan tiến bộ
- `ImprovementDataDTO` - Dữ liệu improvement đầy đủ

#### Full Loop DTOs:
- `FullLearningLoopDataDTO` - Kết hợp Layer 1, 2, 3
- `FullLearningLoopResponse` - Response full loop

### Services

#### LearningLoopService
**Location**: `org.example.estudebackendspring.service.LearningLoopService`

**Methods**:
- `getLearningFeedback(FeedbackRequest)` - Layer 1
- `getLearningRecommendation(RecommendationRequest)` - Layer 2
- `generatePracticeQuiz(PracticeQuizRequest)` - Layer 3
- `evaluateImprovement(ImprovementRequest)` - Layer 4
- `runFullLearningLoop(FeedbackRequest)` - Full Loop

### Controller

#### AIAnalysisController
**Location**: `org.example.estudebackendspring.controller.AIAnalysisController`

**New Endpoints**:
- `POST /api/ai/learning-feedback`
- `POST /api/ai/learning-recommendation`
- `POST /api/ai/generate-practice-quiz`
- `POST /api/ai/improvement-evaluation`
- `POST /api/ai/full-learning-loop`
 
**Student Self-Serve (GET) Endpoints**:
- `GET /api/ai/me/feedback/latest` – Kết quả Layer 1 mới nhất của user hiện tại
- `GET /api/ai/me/recommendation/latest` – Kết quả Layer 2 mới nhất
- `GET /api/ai/me/quiz/latest` – Kết quả Layer 3 mới nhất
- `GET /api/ai/me/improvement/latest` – Kết quả Layer 4 mới nhất
- `GET /api/ai/me/dashboard` – Gộp 4 layer để FE hiển thị nhanh

### Entity & Enums

#### AnalysisType (Updated)
**Location**: `org.example.estudebackendspring.enums.AnalysisType`

**New Values**:
- `LEARNING_FEEDBACK`
- `LEARNING_RECOMMENDATION`
- `PRACTICE_QUIZ`
- `IMPROVEMENT_EVALUATION`
- `FULL_LEARNING_LOOP`

## 📡 API Usage Examples

### 1. Layer 1: Learning Feedback

```bash
POST http://localhost:8080/api/ai/learning-feedback
Content-Type: application/json

{
  "assignment_id": "TEST_001",
  "student_name": "Nguyễn Văn A",
  "subject": "Toán học 12",
  "questions": [
    {
      "question": "Tập xác định của hàm số y = √(x - 2) là:",
      "options": [
        "(-∞; 2)",
        "(2; +∞)",
        "[2; +∞)",
        "R"
      ],
      "correct_answer": 2,
      "student_answer": 3
    }
  ]
}
```

### 2. Layer 2: Learning Recommendation

```bash
POST http://localhost:8080/api/ai/learning-recommendation
Content-Type: application/json

{
  "feedback_data": {
    "student_name": "Nguyễn Văn A",
    "subject": "Toán học 12",
    "summary": {
      "total_questions": 5,
      "correct_count": 2,
      "accuracy_percentage": 40.0
    },
    "feedback": [...]
  }
}
```

### 3. Layer 3: Generate Practice Quiz

```bash
POST http://localhost:8080/api/ai/generate-practice-quiz
Content-Type: application/json

{
  "subject": "Toán học 12",
  "topics": [
    "Miền xác định của hàm căn thức",
    "Cực trị hàm bậc hai"
  ],
  "num_questions": 5,
  "difficulty": "mixed"
}
```

### 4. Layer 4: Improvement Evaluation

```bash
POST http://localhost:8080/api/ai/improvement-evaluation
Content-Type: application/json

{
  "student_id": 17,
  "subject": "Toán học 12",
  "previous_results": [
    {
      "topic": "Miền xác định của hàm căn thức",
      "accuracy": 0.4
    }
  ],
  "new_results": [
    {
      "topic": "Miền xác định của hàm căn thức",
      "accuracy": 0.9
    }
  ]
}
```

### 5. Full Learning Loop

```bash
POST http://localhost:8080/api/ai/full-learning-loop
Content-Type: application/json

{
  "assignment_id": "TEST_001",
  "student_name": "Nguyễn Văn A",
  "subject": "Toán học 12",
  "questions": [...]
}
```

### 6. Student GET – Dashboard tổng hợp

```bash
GET http://localhost:8080/api/ai/me/dashboard
```

Response mẫu:
```json
{
  "success": true,
  "data": {
    "feedback": { "resultId": 1, "detailedAnalysis": { "data": { "summary": {"accuracy_percentage": 60.0}, "feedback": [...] } } },
    "recommendation": { "resultId": 2, "detailedAnalysis": { "weak_topics": [...], "overall_advice": "..." } },
    "practice_quiz": { "resultId": 3, "detailedAnalysis": { "questions": [...] } },
    "improvement": { "resultId": 4, "detailedAnalysis": { "topics": [...], "overall_improvement": {"improvement_percentage": "+35%"} } }
  }
}
```

Các GET lẻ (`/me/*/latest`) trả về trực tiếp bản ghi `AIAnalysisResult` gần nhất cho layer tương ứng.

## ⚙️ Configuration

### application.properties

```properties
# AI Service URL
ai.service.url=http://127.0.0.1:8000
```

## 🗄️ Database

Tất cả requests và responses được tự động lưu vào database:

### Tables:
- `ai_analysis_requests` - Lưu request gửi đến AI
- `ai_analysis_results` - Lưu response từ AI

### Fields Tracked:
- Request type (AnalysisType enum)
- Request payload (JSON)
- Response data (JSON)
- Timestamp
- Student reference (nếu có)
- Comments/errors

## 🔍 Logging

Service sử dụng SLF4J logging:

```java
log.info("Getting learning feedback for assignment: {}, student: {}", assignmentId, studentName);
log.error("Error getting learning feedback", ex);
```

## 🚀 Deployment Notes

### Build & Run

```bash
# Build project
./gradlew build

# Run application
./gradlew bootRun
```

### Docker Support

```bash
# Build Docker image
docker build -t estude-backend .

# Run container
docker run -p 8080:8080 estude-backend
```

## 🧪 Testing

### Health Check

```bash
GET http://localhost:8080/api/ai/health
```

Response:
```json
{
  "status": "UP",
  "service": "Spring Boot API",
  "timestamp": "2025-01-15T10:30:00"
}
```

## 📊 Data Flow

```
Frontend → Spring Boot Controller → LearningLoopService → Python AI Service
                ↓                           ↓                      ↓
          Save to DB                  Process Logic         AI Processing
                ↓                           ↓                      ↓
          AIAnalysisRequest ← AIAnalysisResult ← AI Response
```

## 💡 Best Practices

### 1. Error Handling
- Tất cả exceptions được catch và log
- Trả về HTTP 500 với null body khi có lỗi
- Request vẫn được lưu vào DB kể cả khi có lỗi

### 2. Transaction Management
- Sử dụng `@Transactional` cho tất cả service methods
- Đảm bảo consistency giữa request và result

### 3. Logging
- Log đầy đủ thông tin request/response
- Log errors với stack trace

### 4. Performance
- Sử dụng `RestTemplate` với connection pooling
- Cache ObjectMapper instances

## 🔧 Troubleshooting

### AI Service Connection Error

**Problem**: Cannot connect to AI service

**Solution**:
1. Check `ai.service.url` in application.properties
2. Verify AI service is running
3. Check network connectivity

### JSON Parsing Error

**Problem**: Cannot parse JSON from AI service

**Solution**:
1. Verify DTO field names match AI response
2. Check Jackson annotations (`@JsonProperty`)
3. Enable detailed logging

### Database Save Error

**Problem**: Cannot save to database

**Solution**:
1. Check database connection
2. Verify entity mappings
3. Check JsonNodeConverter is working

## 📝 Changelog

### v1.0.0 (Current)
- ✅ Added Learning Loop AI integration
- ✅ Created 20+ DTOs for all layers
- ✅ Implemented LearningLoopService
- ✅ Added 5 new API endpoints
- ✅ Updated AnalysisType enum
- ✅ Full database tracking support

## 🤝 Contributing

Khi thêm tính năng mới:
1. Tạo DTOs trong package `dto.learning`
2. Thêm method vào `LearningLoopService`
3. Thêm endpoint vào `AIAnalysisController`
4. Update AnalysisType enum nếu cần
5. Test thoroughly

## 📧 Support

Nếu có vấn đề, vui lòng:
1. Check logs trong console
2. Check database records
3. Verify AI service status
4. Review this documentation
