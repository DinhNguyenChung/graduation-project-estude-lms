-- ============================================
-- KIỂM TRA CÂU HỎI CHO TOPICS 1 VÀ 2
-- ============================================

-- 1. Kiểm tra TỔNG số câu hỏi (bất kể is_question_bank)
SELECT 
    topic_id,
    COUNT(*) as total_questions
FROM questions
WHERE topic_id IN (1, 2)
GROUP BY topic_id;

-- 2. Kiểm tra theo is_question_bank flag
SELECT 
    topic_id,
    is_question_bank,
    COUNT(*) as count
FROM questions
WHERE topic_id IN (1, 2)
GROUP BY topic_id, is_question_bank
ORDER BY topic_id, is_question_bank;

-- 3. Chi tiết theo difficulty level và is_question_bank
SELECT 
    topic_id,
    difficulty_level,
    is_question_bank,
    COUNT(*) as count
FROM questions
WHERE topic_id IN (1, 2)
GROUP BY topic_id, difficulty_level, is_question_bank
ORDER BY topic_id, difficulty_level, is_question_bank;

-- 4. Xem chi tiết một vài câu hỏi mẫu
SELECT 
    question_id,
    topic_id,
    question_text,
    difficulty_level,
    is_question_bank,
    created_at
FROM questions
WHERE topic_id IN (1, 2)
LIMIT 10;

-- ============================================
-- GIẢI PHÁP: NẾU CÂU HỎI CÓ NHƯNG is_question_bank = false
-- ============================================

-- Chạy câu lệnh này để bật flag cho TẤT CẢ câu hỏi của topics 1 và 2:
-- UPDATE questions 
-- SET is_question_bank = true 
-- WHERE topic_id IN (1, 2);

-- Hoặc chỉ bật cho những câu có đủ 3 mức độ:
-- UPDATE questions 
-- SET is_question_bank = true 
-- WHERE topic_id IN (1, 2)
--   AND difficulty_level IN ('EASY', 'MEDIUM', 'HARD');

-- ============================================
-- KIỂM TRA LẠI SAU KHI UPDATE
-- ============================================

-- Chạy lại query này để xác nhận:
-- SELECT 
--     topic_id,
--     difficulty_level,
--     is_question_bank,
--     COUNT(*) as count
-- FROM questions
-- WHERE topic_id IN (1, 2)
-- GROUP BY topic_id, difficulty_level, is_question_bank
-- ORDER BY topic_id, difficulty_level;

-- Kết quả mong đợi:
-- topic_id | difficulty_level | is_question_bank | count
-- ---------|------------------|------------------|-------
-- 1        | EASY            | true             | >= 2
-- 1        | MEDIUM          | true             | >= 2
-- 1        | HARD            | true             | >= 1
-- 2        | EASY            | true             | >= 2
-- 2        | MEDIUM          | true             | >= 2
-- 2        | HARD            | true             | >= 1
