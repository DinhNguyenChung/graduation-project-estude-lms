-- ============================================
-- DEBUG: Kiểm tra kỹ lưỡng cho topics 1 và 2
-- ============================================
-- Vì is_question_bank = true rồi, nhưng vẫn lỗi
-- Cần kiểm tra các vấn đề khác

-- 1. VERIFY: is_question_bank đúng là true?
SELECT 
    topic_id,
    is_question_bank,
    COUNT(*) as count
FROM questions
WHERE topic_id IN (1, 2)
GROUP BY topic_id, is_question_bank
ORDER BY topic_id, is_question_bank;
-- Expected: Tất cả phải là true

-- 2. KIỂM TRA: Có câu hỏi cho từng mức độ không?
SELECT 
    topic_id,
    difficulty_level,
    COUNT(*) as count
FROM questions
WHERE topic_id IN (1, 2)
  AND is_question_bank = true
GROUP BY topic_id, difficulty_level
ORDER BY topic_id, difficulty_level;
-- Expected: Mỗi topic phải có ít nhất 2 câu EASY, 2 câu MEDIUM, 1 câu HARD

-- 3. KIỂM TRA: Difficulty level có đúng format không?
SELECT DISTINCT difficulty_level
FROM questions
WHERE topic_id IN (1, 2);
-- Expected: 'EASY', 'MEDIUM', 'HARD' (VIẾT HOA, không có khoảng trắng)

-- 4. KIỂM TRA: Có NULL values không?
SELECT 
    COUNT(*) as total,
    COUNT(CASE WHEN difficulty_level IS NULL THEN 1 END) as null_difficulty,
    COUNT(CASE WHEN topic_id IS NULL THEN 1 END) as null_topic,
    COUNT(CASE WHEN is_question_bank IS NULL THEN 1 END) as null_is_question_bank
FROM questions
WHERE topic_id IN (1, 2);

-- 5. KIỂM TRA CHI TIẾT: Xem một số câu hỏi mẫu
SELECT 
    question_id,
    topic_id,
    SUBSTRING(question_text, 1, 50) as question_preview,
    difficulty_level,
    is_question_bank,
    created_at
FROM questions
WHERE topic_id IN (1, 2)
ORDER BY topic_id, difficulty_level
LIMIT 20;

-- 6. KIỂM TRA: Options có đầy đủ không?
SELECT 
    q.question_id,
    q.topic_id,
    SUBSTRING(q.question_text, 1, 40) as question,
    q.difficulty_level,
    COUNT(qo.option_id) as option_count,
    SUM(CASE WHEN qo.is_correct = true THEN 1 ELSE 0 END) as correct_count
FROM questions q
LEFT JOIN question_options qo ON q.question_id = qo.question_id
WHERE q.topic_id IN (1, 2)
  AND q.is_question_bank = true
GROUP BY q.question_id, q.topic_id, q.question_text, q.difficulty_level
HAVING COUNT(qo.option_id) = 0  -- Tìm câu hỏi KHÔNG có options
    OR SUM(CASE WHEN qo.is_correct = true THEN 1 ELSE 0 END) = 0;  -- Hoặc không có đáp án đúng

-- 7. KIỂM TRA DETAIL: Số câu theo từng mức độ và topic
SELECT 
    topic_id,
    difficulty_level,
    is_question_bank,
    COUNT(*) as available_questions,
    -- Requirement cho 5 câu hỏi chia 2 topics:
    CASE 
        WHEN difficulty_level = 'EASY' THEN '2 câu cần' 
        WHEN difficulty_level = 'MEDIUM' THEN '2 câu cần'
        WHEN difficulty_level = 'HARD' THEN '1 câu cần'
    END as required,
    CASE 
        WHEN difficulty_level = 'EASY' AND COUNT(*) >= 2 THEN '✅'
        WHEN difficulty_level = 'MEDIUM' AND COUNT(*) >= 2 THEN '✅'
        WHEN difficulty_level = 'HARD' AND COUNT(*) >= 1 THEN '✅'
        WHEN COUNT(*) = 0 THEN '❌ KHÔNG CÓ CÂU NÀO'
        ELSE '⚠️ KHÔNG ĐỦ'
    END as status
FROM questions
WHERE topic_id IN (1, 2)
  AND is_question_bank = true
GROUP BY topic_id, difficulty_level, is_question_bank
ORDER BY topic_id, 
    CASE difficulty_level 
        WHEN 'EASY' THEN 1 
        WHEN 'MEDIUM' THEN 2 
        WHEN 'HARD' THEN 3 
    END;

-- 8. KIỂM TRA: Topic có tồn tại không?
SELECT topic_id, name, subject_id
FROM topics
WHERE topic_id IN (1, 2);

-- 9. KIỂM TRA: Subject có tồn tại không?
SELECT subject_id, name
FROM subjects
WHERE subject_id = 1;

-- 10. KIỂM TRA TOÀN BỘ: Trạng thái hiện tại
SELECT 
    'Topic ' || topic_id as info,
    difficulty_level,
    COUNT(*) as count
FROM questions
WHERE topic_id IN (1, 2)
  AND is_question_bank = true
GROUP BY ROLLUP(topic_id, difficulty_level)
ORDER BY topic_id NULLS LAST, difficulty_level NULLS LAST;
