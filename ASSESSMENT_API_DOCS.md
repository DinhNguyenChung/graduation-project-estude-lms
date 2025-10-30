# API Đánh Giá Năng Lực (Assessment) - Backend Documentation

## Tổng Quan

API này xử lý việc sinh câu hỏi ngẫu nhiên cho bài đánh giá năng lực của học sinh. Backend sẽ chịu tr책nhiệm:
- Phân phối câu hỏi đều cho các topics
- Xử lý tỷ lệ mức độ khó (nếu chọn "Hỗn hợp")
- Random và shuffle câu hỏi
- Đảm bảo không trùng lặp

## API Endpoint

### POST `/api/assessment/generate-questions`

Tạo bộ câu hỏi ngẫu nhiên cho bài đánh giá dựa trên các topics và cấu hình đã chọn.

---

## Request

### Headers
```
Content-Type: application/json
Authorization: Bearer {token}
```

### Request Body

```json
{
  "studentId": 123,
  "subjectId": 1,
  "topicIds": [1, 2, 3, 5, 8],
  "numQuestions": 20,
  "difficulty": "mixed",
  "gradeLevel": "GRADE_10"
}
```

### Parameters

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `studentId` | Integer | Yes | ID của học sinh đang làm bài |
| `subjectId` | Integer | Yes | ID môn học |
| `topicIds` | Array<Integer> | Yes | Danh sách ID các topics đã chọn |
| `numQuestions` | Integer | Yes | Tổng số câu hỏi cần sinh (phải ≥ số topics) |
| `difficulty` | String | Yes | Mức độ: `"easy"`, `"medium"`, `"hard"`, `"mixed"` |
| `gradeLevel` | String | Yes | Khối học: `"GRADE_10"`, `"GRADE_11"`, `"GRADE_12"` |

### Validation Rules

1. **numQuestions >= topicIds.length**
   - Ít nhất 1 câu hỏi cho mỗi topic
   - Trả về error nếu vi phạm

2. **difficulty values:**
   - `"easy"`: Chỉ lấy câu hỏi mức DỄ
   - `"medium"`: Chỉ lấy câu hỏi mức TRUNG BÌNH
   - `"hard"`: Chỉ lấy câu hỏi mức KHÓ
   - `"mixed"`: Hỗn hợp với tỷ lệ 40% DỄ, 40% TRUNG BÌNH, 20% KHÓ

---

## Response

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Đã tạo bộ câu hỏi thành công",
  "data": {
    "assessmentId": "uuid-or-auto-generated-id",
    "subjectId": 1,
    "subjectName": "Toán",
    "totalQuestions": 20,
    "difficulty": "mixed",
    "questions": [
      {
        "questionId": 45,
        "questionText": "Mệnh đề nào sau đây đúng?",
        "difficultyLevel": "EASY",
        "topicId": 1,
        "topicName": "Mệnh đề",
        "options": [
          {
            "optionId": 178,
            "optionText": "2 + 2 = 4",
            "isCorrect": true
          },
          {
            "optionId": 179,
            "optionText": "3 + 3 = 5",
            "isCorrect": false
          },
          {
            "optionId": 180,
            "optionText": "1 + 1 = 3",
            "isCorrect": false
          },
          {
            "optionId": 181,
            "optionText": "5 - 2 = 4",
            "isCorrect": false
          }
        ],
        "explanation": "Giải thích đáp án (optional)"
      },
      {
        "questionId": 52,
        "questionText": "Cho tập hợp A = {1, 2, 3}...",
        "difficultyLevel": "MEDIUM",
        "topicId": 2,
        "topicName": "Tập hợp",
        "options": [
          // ... 4 options
        ],
        "explanation": null
      }
      // ... 18 câu khác
    ],
    "distribution": {
      "byTopic": {
        "1": 4,
        "2": 4,
        "3": 4,
        "5": 4,
        "8": 4
      },
      "byDifficulty": {
        "EASY": 8,
        "MEDIUM": 8,
        "HARD": 4
      }
    },
    "createdAt": "2025-10-30T10:30:00Z"
  }
}
```

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `assessmentId` | String | ID duy nhất của bài đánh giá (để lưu kết quả sau này) |
| `questions` | Array | Mảng câu hỏi đã được shuffle ngẫu nhiên |
| `questions[].questionId` | Integer | ID câu hỏi |
| `questions[].questionText` | String | Nội dung câu hỏi |
| `questions[].difficultyLevel` | String | `"EASY"`, `"MEDIUM"`, `"HARD"` |
| `questions[].topicId` | Integer | ID chủ đề |
| `questions[].topicName` | String | Tên chủ đề |
| `questions[].options` | Array | 4 đáp án (đã shuffle) |
| `questions[].options[].optionId` | Integer | ID đáp án |
| `questions[].options[].optionText` | String | Nội dung đáp án |
| `questions[].options[].isCorrect` | Boolean | true nếu là đáp án đúng |
| `questions[].explanation` | String/null | Giải thích (optional) |
| `distribution` | Object | Thống kê phân phối câu hỏi |

---

## Algorithm Logic (Backend Implementation)

### 1. Phân Phối Câu Hỏi Theo Topics

```javascript
// Chia đều câu hỏi cho các topics
const baseQuestionsPerTopic = Math.floor(numQuestions / topicIds.length);
const remainder = numQuestions % topicIds.length;

// Phân phối:
// - Mỗi topic được `baseQuestionsPerTopic` câu
// - `remainder` câu còn lại phân ngẫu nhiên cho các topics
```

**Ví dụ:**
- 20 câu hỏi, 5 topics → mỗi topic: 4 câu
- 22 câu hỏi, 5 topics → 3 topics được 5 câu, 2 topics được 4 câu

### 2. Phân Phối Theo Mức Độ (Mixed Mode)

Với mỗi topic đã biết số câu hỏi:

```javascript
if (difficulty === "mixed") {
  const easyCount = Math.ceil(topicQuestionCount * 0.4);    // 40%
  const mediumCount = Math.ceil(topicQuestionCount * 0.4);  // 40%
  const hardCount = topicQuestionCount - easyCount - mediumCount; // 20%
  
  // Lấy random từ 3 độ khó
  questions = [
    ...getRandomQuestions(topicId, "EASY", easyCount),
    ...getRandomQuestions(topicId, "MEDIUM", mediumCount),
    ...getRandomQuestions(topicId, "HARD", hardCount)
  ];
} else {
  // Single difficulty
  const difficultyMap = {
    "easy": "EASY",
    "medium": "MEDIUM",
    "hard": "HARD"
  };
  questions = getRandomQuestions(topicId, difficultyMap[difficulty], topicQuestionCount);
}
```

### 3. Xử Lý Fallback

Nếu topic không đủ câu hỏi ở mức độ yêu cầu:

```javascript
// Option 1: Lấy từ mức độ khác trong cùng topic
// Option 2: Báo lỗi yêu cầu giảm số câu hỏi
// Option 3: Bỏ qua và phân phối lại cho topics khác

// Recommended: Option 1
if (questionsFromDifficulty.length < requiredCount) {
  // Lấy thêm từ mức khác
  const remaining = requiredCount - questionsFromDifficulty.length;
  const fallbackQuestions = getRandomQuestionsFromOtherDifficulties(topicId, remaining);
  questions = [...questionsFromDifficulty, ...fallbackQuestions];
}
```

### 4. Shuffle Final Questions

```java
// Java example
Collections.shuffle(allQuestions);

// hoặc SQL
SELECT * FROM questions WHERE ... ORDER BY RAND() LIMIT ?
```

---

## Error Responses

### 400 Bad Request - Validation Error

```json
{
  "success": false,
  "message": "Số câu hỏi phải lớn hơn hoặc bằng số topics",
  "error": {
    "code": "INVALID_QUESTION_COUNT",
    "details": {
      "numQuestions": 10,
      "numTopics": 15,
      "minRequired": 15
    }
  }
}
```

### 404 Not Found - Không đủ câu hỏi

```json
{
  "success": false,
  "message": "Không tìm đủ câu hỏi cho các topics đã chọn",
  "error": {
    "code": "INSUFFICIENT_QUESTIONS",
    "details": {
      "topicId": 3,
      "topicName": "Hàm số bậc nhất",
      "requested": 5,
      "available": 2,
      "difficulty": "HARD"
    }
  }
}
```

### 401 Unauthorized

```json
{
  "success": false,
  "message": "Token không hợp lệ hoặc đã hết hạn"
}
```

## Frontend Integration Points

### 1. Service Call

```javascript
// FE: topicService.js
generateAssessmentQuestions: async (data) => {
  const response = await fetch(`${config.BASE_URL}/api/assessment/generate-questions`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify(data),
  });
  return response.json();
}
```

### 2. Request từ AssessmentTopicSelectionScreen

```javascript
const payload = {
  studentId: user.userId,
  subjectId: subjectId,
  topicIds: selectedTopicIds,
  numQuestions: parseInt(numQuestions),
  difficulty: difficulty, // "easy" | "medium" | "hard" | "mixed"
  gradeLevel: gradeLevel
};
```

### 3. FE chỉ validation:
- numQuestions >= selectedTopics.length
- numQuestions > 0
- Các field required không empty

### 4. Backend xử lý:
- Random questions
- Phân phối đều
- Shuffle
- Trả về ready-to-use questions array

---

## Testing Scenarios

### Test Case 1: Basic Flow
```
Input:
- 5 topics
- 20 questions
- mixed difficulty

Expected:
- 20 questions returned
- Each topic: 4 questions
- Distribution: ~8 EASY, ~8 MEDIUM, ~4 HARD
```

### Test Case 2: Odd Distribution
```
Input:
- 3 topics
- 10 questions
- easy difficulty

Expected:
- 10 EASY questions
- Distribution: 4, 3, 3 or 3, 4, 3 (random)
```

### Test Case 3: Insufficient Questions
```
Input:
- 1 topic with only 5 HARD questions available
- 10 questions requested
- hard difficulty

Expected:
- 404 Error or Fallback to other difficulties
```

### Test Case 4: Edge Case
```
Input:
- 10 topics
- 10 questions (minimum allowed)

Expected:
- 10 questions, 1 per topic
```

---

## Performance Considerations

1. **Database Indexing:**
   - Index on `topic_id, difficulty_level`
   - Improves query speed for random selection

2. **Caching:**
   - Cache frequently used topics' questions
   - TTL: 1 hour

3. **Batch Processing:**
   - Fetch all questions in single query with JOIN
   - Avoid N+1 queries

4. **Rate Limiting:**
   - Max 10 requests/minute per student
   - Prevent spam generation

---

## Security Considerations

1. **Authorization:**
   - Verify studentId matches authenticated user
   - Check student has access to subjectId

2. **Input Sanitization:**
   - Validate all IDs exist in database
   - Prevent SQL injection

3. **Data Exposure:**
   - Never return `isCorrect` flag in initial response
   - Only return after submission

---

## Notes for Backend Developer

- Sử dụng transaction khi tạo assessment session
- Log mọi request để debug
- Consider saving generated questions để student có thể resume
- Implement timeout: 30s max generation time
- Return 503 nếu server quá tải

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-10-30 | Initial API specification |

---
