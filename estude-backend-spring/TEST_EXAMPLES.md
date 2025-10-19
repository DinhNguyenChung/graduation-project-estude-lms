# Learning Loop AI - Test Examples

## Test với cURL

### 1. Health Check

```bash
curl -X GET http://localhost:8080/api/ai/health
```

### 2. Layer 1: Learning Feedback

```bash
curl -X POST http://localhost:8080/api/ai/learning-feedback \
  -H "Content-Type: application/json" \
  -d '{
    "assignment_id": "MATH_001",
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
      },
      {
        "question": "Đạo hàm của hàm số y = x² là:",
        "options": [
          "x",
          "2x",
          "x²",
          "2"
        ],
        "correct_answer": 1,
        "student_answer": 1
      }
    ]
  }'
```

### 3. Layer 3: Generate Practice Quiz

```bash
curl -X POST http://localhost:8080/api/ai/generate-practice-quiz \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Toán học 12",
    "topics": [
      "Miền xác định của hàm căn thức",
      "Cực trị hàm bậc hai"
    ],
    "num_questions": 5,
    "difficulty": "mixed"
  }'
```

### 4. Layer 4: Improvement Evaluation

```bash
curl -X POST http://localhost:8080/api/ai/improvement-evaluation \
  -H "Content-Type: application/json" \
  -d '{
    "student_id": 17,
    "subject": "Toán học 12",
    "previous_results": [
      {
        "topic": "Miền xác định của hàm căn thức",
        "accuracy": 0.4
      },
      {
        "topic": "Cực trị hàm bậc hai",
        "accuracy": 0.6
      }
    ],
    "new_results": [
      {
        "topic": "Miền xác định của hàm căn thức",
        "accuracy": 0.9
      },
      {
        "topic": "Cực trị hàm bậc hai",
        "accuracy": 0.8
      }
    ]
  }'
```

### 5. Full Learning Loop

```bash
curl -X POST http://localhost:8080/api/ai/full-learning-loop \
  -H "Content-Type: application/json" \
  -d '{
    "assignment_id": "MATH_001",
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
  }'
```

## Test với Postman

### Import Collection

1. Mở Postman
2. Click **Import**
3. Paste JSON bên dưới

```json
{
  "info": {
    "name": "Learning Loop AI",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Health Check",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8080/api/ai/health",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "ai", "health"]
        }
      }
    },
    {
      "name": "Layer 1 - Learning Feedback",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"assignment_id\": \"MATH_001\",\n  \"student_name\": \"Nguyễn Văn A\",\n  \"subject\": \"Toán học 12\",\n  \"questions\": [\n    {\n      \"question\": \"Tập xác định của hàm số y = √(x - 2) là:\",\n      \"options\": [\n        \"(-∞; 2)\",\n        \"(2; +∞)\",\n        \"[2; +∞)\",\n        \"R\"\n      ],\n      \"correct_answer\": 2,\n      \"student_answer\": 3\n    }\n  ]\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/ai/learning-feedback",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "ai", "learning-feedback"]
        }
      }
    },
    {
      "name": "Layer 3 - Generate Practice Quiz",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"subject\": \"Toán học 12\",\n  \"topics\": [\n    \"Miền xác định của hàm căn thức\"\n  ],\n  \"num_questions\": 5,\n  \"difficulty\": \"mixed\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/ai/generate-practice-quiz",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "ai", "generate-practice-quiz"]
        }
      }
    },
    {
      "name": "Layer 4 - Improvement Evaluation",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"student_id\": 17,\n  \"subject\": \"Toán học 12\",\n  \"previous_results\": [\n    {\n      \"topic\": \"Miền xác định\",\n      \"accuracy\": 0.4\n    }\n  ],\n  \"new_results\": [\n    {\n      \"topic\": \"Miền xác định\",\n      \"accuracy\": 0.9\n    }\n  ]\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/ai/improvement-evaluation",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "ai", "improvement-evaluation"]
        }
      }
    },
    {
      "name": "Full Learning Loop",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"assignment_id\": \"MATH_001\",\n  \"student_name\": \"Nguyễn Văn A\",\n  \"subject\": \"Toán học 12\",\n  \"questions\": [\n    {\n      \"question\": \"Tập xác định của hàm số y = √(x - 2) là:\",\n      \"options\": [\n        \"(-∞; 2)\",\n        \"(2; +∞)\",\n        \"[2; +∞)\",\n        \"R\"\n      ],\n      \"correct_answer\": 2,\n      \"student_answer\": 3\n    }\n  ]\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/ai/full-learning-loop",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "ai", "full-learning-loop"]
        }
      }
    }
  ]
}
```

## Test với JavaScript/Fetch API

### Layer 1: Learning Feedback

```javascript
const getLearningFeedback = async (testData) => {
  const response = await fetch('http://localhost:8080/api/ai/learning-feedback', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      assignment_id: testData.assignmentId,
      student_name: testData.studentName,
      subject: testData.subject,
      questions: testData.questions
    })
  });
  
  const result = await response.json();
  
  if (result.success) {
    console.log('Feedback:', result.data);
    return result.data;
  } else {
    console.error('Error getting feedback');
    return null;
  }
};

// Usage
const testData = {
  assignmentId: 'MATH_001',
  studentName: 'Nguyễn Văn A',
  subject: 'Toán học 12',
  questions: [
    {
      question: 'Tập xác định của hàm số y = √(x - 2) là:',
      options: ['(-∞; 2)', '(2; +∞)', '[2; +∞)', 'R'],
      correct_answer: 2,
      student_answer: 3
    }
  ]
};

getLearningFeedback(testData);
```

### Full Learning Loop

```javascript
const runFullLoop = async (testData) => {
  try {
    const response = await fetch('http://localhost:8080/api/ai/full-learning-loop', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(testData)
    });
    
    const result = await response.json();
    
    if (result.success) {
      const { layer1_feedback, layer2_recommendation, layer3_practice_quiz } = result.data;
      
      // Display feedback
      console.log('Feedback:', layer1_feedback);
      
      // Display recommendations
      console.log('Recommendations:', layer2_recommendation);
      
      // Display practice quiz
      console.log('Practice Quiz:', layer3_practice_quiz);
      
      return result.data;
    }
  } catch (error) {
    console.error('Error:', error);
  }
};
```

## Expected Responses

### Layer 1 Response

```json
{
  "success": true,
  "data": {
    "assignment_id": "MATH_001",
    "student_name": "Nguyễn Văn A",
    "subject": "Toán học 12",
    "timestamp": "2025-01-15T10:30:00",
    "summary": {
      "total_questions": 2,
      "correct_count": 1,
      "accuracy_percentage": 50.0
    },
    "feedback": [
      {
        "question": "Tập xác định của hàm số y = √(x - 2) là:",
        "student_answer": "R",
        "correct_answer": "[2; +∞)",
        "is_correct": false,
        "explanation": "Hàm căn chỉ xác định khi biểu thức trong căn ≥ 0...",
        "topic": "Miền xác định của hàm số",
        "subtopic": "Hàm căn thức",
        "difficulty_level": "Trung bình"
      }
    ]
  }
}
```

### Layer 3 Response

```json
{
  "success": true,
  "data": {
    "subject": "Toán học 12",
    "topics": ["Miền xác định của hàm căn thức"],
    "difficulty": "mixed",
    "total_questions": 5,
    "generated_at": "2025-01-15T10:40:00",
    "questions": [
      {
        "topic": "Miền xác định của hàm căn thức",
        "subtopic": "Căn bậc hai",
        "question": "Tập xác định của hàm số y = √(2x - 6) là:",
        "options": ["A. x ≥ 3", "B. x ≤ 3", "C. x > 3", "D. x ∈ R"],
        "correct_answer": 0,
        "explanation": "Hàm căn xác định khi 2x - 6 ≥ 0...",
        "difficulty_level": "Dễ",
        "study_hint": "Nhớ điều kiện: biểu thức trong căn phải ≥ 0"
      }
    ]
  }
}
```

## Debugging

### Check Logs

```bash
# View Spring Boot logs
tail -f logs/spring-boot-application.log

# Or in console when running
./gradlew bootRun
```

### Check Database

```sql
-- Check saved requests
SELECT * FROM ai_analysis_requests 
WHERE analysis_type = 'LEARNING_FEEDBACK' 
ORDER BY request_date DESC 
LIMIT 10;

-- Check saved results
SELECT * FROM ai_analysis_results 
WHERE generated_at > NOW() - INTERVAL '1 hour'
ORDER BY generated_at DESC;
```

### Test AI Service Connection

```bash
# Test if AI service is running
curl http://127.0.0.1:8000/health

# Test AI endpoints directly
curl -X POST http://127.0.0.1:8000/api/ai/learning-feedback \
  -H "Content-Type: application/json" \
  -d '{"assignment_id": "TEST"}'
```
