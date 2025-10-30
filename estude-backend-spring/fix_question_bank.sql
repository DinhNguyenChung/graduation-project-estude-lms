-- ============================================
-- FIX: ENABLE QUESTION BANK FOR TOPICS 1 & 2
-- ============================================
-- Date: October 30, 2025
-- Issue: API trả lỗi "fromIndex(0) > toIndex(-1)" vì is_question_bank = false
-- Solution: Set is_question_bank = true cho các câu hỏi của topics 1 và 2

-- ============================================
-- BƯỚC 1: KIỂM TRA TRƯỚC KHI UPDATE
-- ============================================

-- Xem tổng số câu hỏi
SELECT 
    'BEFORE UPDATE' as status,
    topic_id,
    is_question_bank,
    COUNT(*) as count
FROM questions
WHERE topic_id IN (1, 2)
GROUP BY topic_id, is_question_bank
ORDER BY topic_id, is_question_bank;

-- Xem chi tiết theo difficulty
SELECT 
    'BEFORE UPDATE' as status,
    topic_id,
    difficulty_level,
    is_question_bank,
    COUNT(*) as count
FROM questions
WHERE topic_id IN (1, 2)
GROUP BY topic_id, difficulty_level, is_question_bank
ORDER BY topic_id, difficulty_level, is_question_bank;

-- ============================================
-- BƯỚC 2: BACKUP DATA (RECOMMENDED)
-- ============================================

-- Tạo bảng backup (optional nhưng recommended)
-- CREATE TABLE questions_backup_20251030 AS 
-- SELECT * FROM questions WHERE topic_id IN (1, 2);

-- ============================================
-- BƯỚC 3: UPDATE is_question_bank = true
-- ============================================

-- Update TẤT CẢ câu hỏi của topics 1 và 2
UPDATE questions 
SET is_question_bank = true 
WHERE topic_id IN (1, 2);

-- Verify số dòng bị ảnh hưởng
-- Expected: ~100 rows (50 câu x 2 topics)

-- ============================================
-- BƯỚC 4: KIỂM TRA SAU KHI UPDATE
-- ============================================

-- Kiểm tra lại tổng số
SELECT 
    'AFTER UPDATE' as status,
    topic_id,
    is_question_bank,
    COUNT(*) as count
FROM questions
WHERE topic_id IN (1, 2)
GROUP BY topic_id, is_question_bank
ORDER BY topic_id, is_question_bank;

-- Kiểm tra chi tiết theo difficulty
SELECT 
    'AFTER UPDATE' as status,
    topic_id,
    difficulty_level,
    is_question_bank,
    COUNT(*) as count
FROM questions
WHERE topic_id IN (1, 2)
GROUP BY topic_id, difficulty_level, is_question_bank
ORDER BY topic_id, difficulty_level, is_question_bank;

-- ============================================
-- BƯỚC 5: VERIFY KẾT QUẢ
-- ============================================

-- Kết quả mong đợi cho MIXED difficulty (5 câu hỏi):
-- - EASY: 2 câu (40%)
-- - MEDIUM: 2 câu (40%)
-- - HARD: 1 câu (20%)
-- Mỗi topic cần có ít nhất: 3 câu (5 ÷ 2 = 2.5, làm tròn lên 3)

-- Check xem mỗi topic có đủ câu không
SELECT 
    topic_id,
    difficulty_level,
    COUNT(*) as available_questions,
    CASE 
        WHEN difficulty_level = 'EASY' THEN 2
        WHEN difficulty_level = 'MEDIUM' THEN 2
        WHEN difficulty_level = 'HARD' THEN 1
    END as required_questions,
    CASE 
        WHEN difficulty_level = 'EASY' AND COUNT(*) >= 2 THEN '✅ OK'
        WHEN difficulty_level = 'MEDIUM' AND COUNT(*) >= 2 THEN '✅ OK'
        WHEN difficulty_level = 'HARD' AND COUNT(*) >= 1 THEN '✅ OK'
        ELSE '❌ INSUFFICIENT'
    END as status
FROM questions
WHERE topic_id IN (1, 2)
  AND is_question_bank = true
GROUP BY topic_id, difficulty_level
ORDER BY topic_id, difficulty_level;

-- ============================================
-- BƯỚC 6: TEST VỚI API
-- ============================================

-- Sau khi chạy UPDATE, test lại với cURL:
/*
curl -X POST http://192.168.100.210:8080/api/assessment/generate-questions \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 53,
    "subjectId": 1,
    "topicIds": [1, 2],
    "numQuestions": 5,
    "difficulty": "mixed",
    "gradeLevel": "GRADE_10"
  }'
*/

-- ============================================
-- ROLLBACK (NẾU CẦN)
-- ============================================

-- Nếu cần rollback về trạng thái cũ:
-- UPDATE questions 
-- SET is_question_bank = false 
-- WHERE topic_id IN (1, 2);

-- Hoặc restore từ backup:
-- UPDATE questions q
-- SET is_question_bank = b.is_question_bank
-- FROM questions_backup_20251030 b
-- WHERE q.question_id = b.question_id;

-- ============================================
-- NOTES
-- ============================================

-- 1. is_question_bank = true nghĩa là câu hỏi được dùng cho Assessment
-- 2. is_question_bank = false nghĩa là câu hỏi chỉ dùng cho Assignment
-- 3. Nếu muốn chọn lọc câu hỏi nào vào question bank:
--    UPDATE questions SET is_question_bank = true 
--    WHERE question_id IN (1, 2, 3, ...);
-- 4. Best practice: Mỗi topic nên có ít nhất 20-30 câu trong question bank
--    với tỷ lệ 40% EASY, 40% MEDIUM, 20% HARD

-- ============================================
-- TROUBLESHOOTING
-- ============================================

-- Nếu vẫn lỗi sau khi update, check:

-- 1. Verify question có options không
SELECT 
    q.question_id,
    q.question_text,
    COUNT(qo.option_id) as option_count
FROM questions q
LEFT JOIN question_options qo ON q.question_id = qo.question_id
WHERE q.topic_id IN (1, 2)
  AND q.is_question_bank = true
GROUP BY q.question_id, q.question_text
HAVING COUNT(qo.option_id) < 2;
-- Nếu có kết quả, những câu hỏi này thiếu options!

-- 2. Verify có câu trả lời đúng không
SELECT 
    q.question_id,
    q.question_text,
    COUNT(CASE WHEN qo.is_correct = true THEN 1 END) as correct_options
FROM questions q
LEFT JOIN question_options qo ON q.question_id = qo.question_id
WHERE q.topic_id IN (1, 2)
  AND q.is_question_bank = true
GROUP BY q.question_id, q.question_text
HAVING COUNT(CASE WHEN qo.is_correct = true THEN 1 END) = 0;
-- Nếu có kết quả, những câu hỏi này không có đáp án đúng!

-- ============================================
-- END OF MIGRATION SCRIPT
-- ============================================
