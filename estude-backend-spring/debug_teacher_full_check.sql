-- COMPREHENSIVE DEBUG QUERY FOR TEACHER ID = 2

-- ========== STEP 1: CHECK TEACHER EXISTS ==========
SELECT 'STEP 1: Teacher Info' as step;
SELECT u.user_id, u.full_name, t.teacher_code, t.is_admin
FROM users u
JOIN teachers t ON u.user_id = t.user_id
WHERE u.user_id = 2;

-- ========== STEP 2: CHECK CLASS_SUBJECTS ==========
SELECT 'STEP 2: ClassSubjects for Teacher' as step;
SELECT 
    cs.class_subject_id,
    cs.teacher_id,
    cs.term_id,
    cs.subject_id,
    s.name as subject_name
FROM class_subjects cs
LEFT JOIN subjects s ON cs.subject_id = s.subject_id
WHERE cs.teacher_id = 2;

-- ========== STEP 3: CHECK TERMS FOR THOSE CLASS_SUBJECTS ==========
SELECT 'STEP 3: Terms linked to ClassSubjects' as step;
SELECT 
    cs.class_subject_id,
    t.term_id,
    t.name as term_name,
    t.class_id,
    t.begin_date,
    t.end_date
FROM class_subjects cs
LEFT JOIN terms t ON cs.term_id = t.term_id
WHERE cs.teacher_id = 2;

-- ========== STEP 4: CHECK CLASSES FOR THOSE TERMS ==========
SELECT 'STEP 4: Classes linked to Terms' as step;
SELECT 
    cs.class_subject_id,
    t.term_id,
    c.class_id,
    c.name as class_name,
    c.grade_level
FROM class_subjects cs
LEFT JOIN terms t ON cs.term_id = t.term_id
LEFT JOIN classes c ON t.class_id = c.class_id
WHERE cs.teacher_id = 2;

-- ========== STEP 5: CHECK SUBJECT_GRADES ==========
SELECT 'STEP 5: SubjectGrades for ClassSubjects' as step;
SELECT 
    cs.class_subject_id,
    s.name as subject_name,
    c.name as class_name,
    COUNT(sg.subject_grade_id) as grade_count,
    COUNT(CASE WHEN sg.actual_average IS NOT NULL THEN 1 END) as grades_with_average,
    AVG(sg.actual_average) as avg_score
FROM class_subjects cs
LEFT JOIN subjects s ON cs.subject_id = s.subject_id
LEFT JOIN terms t ON cs.term_id = t.term_id
LEFT JOIN classes c ON t.class_id = c.class_id
LEFT JOIN subject_grades sg ON cs.class_subject_id = sg.class_subject_id
WHERE cs.teacher_id = 2
GROUP BY cs.class_subject_id, s.name, c.name;

-- ========== STEP 6: CHECK FOR NULL VALUES ==========
SELECT 'STEP 6: Check for NULL values' as step;
SELECT 
    cs.class_subject_id,
    CASE WHEN cs.term_id IS NULL THEN 'NULL TERM_ID' ELSE 'OK' END as term_status,
    CASE WHEN t.term_id IS NULL THEN 'TERM NOT FOUND' ELSE 'OK' END as term_exists,
    CASE WHEN t.class_id IS NULL THEN 'NULL CLASS_ID in TERM' ELSE 'OK' END as class_id_status,
    CASE WHEN c.class_id IS NULL THEN 'CLASS NOT FOUND' ELSE 'OK' END as class_exists
FROM class_subjects cs
LEFT JOIN terms t ON cs.term_id = t.term_id
LEFT JOIN classes c ON t.class_id = c.class_id
WHERE cs.teacher_id = 2;

-- ========== STEP 7: DETAILED GRADES CHECK ==========
SELECT 'STEP 7: Detailed Grades Data' as step;
SELECT 
    cs.class_subject_id,
    sg.subject_grade_id,
    sg.student_id,
    sg.actual_average,
    CASE 
        WHEN sg.actual_average IS NULL THEN 'NULL'
        WHEN sg.actual_average = 0 THEN 'ZERO'
        WHEN sg.actual_average < 5.0 THEN 'FAIL'
        WHEN sg.actual_average < 9.0 THEN 'PASS'
        ELSE 'EXCELLENT'
    END as performance_category
FROM class_subjects cs
LEFT JOIN subject_grades sg ON cs.class_subject_id = sg.class_subject_id
WHERE cs.teacher_id = 2
ORDER BY cs.class_subject_id, sg.actual_average DESC
LIMIT 50;

-- ========== STEP 8: FINAL EXPECTED DATA ==========
SELECT 'STEP 8: What the API should return' as step;
SELECT 
    c.class_id,
    c.name as class_name,
    c.grade_level,
    s.name as subject_name,
    COUNT(DISTINCT sg.student_id) as student_count,
    ROUND(AVG(sg.actual_average)::numeric, 2) as avg_score,
    ROUND((COUNT(CASE WHEN sg.actual_average >= 5.0 THEN 1 END)::numeric / 
           NULLIF(COUNT(sg.subject_grade_id), 0) * 100), 2) as pass_rate,
    ROUND((COUNT(CASE WHEN sg.actual_average >= 9.0 THEN 1 END)::numeric / 
           NULLIF(COUNT(sg.subject_grade_id), 0) * 100), 2) as excellent_rate
FROM class_subjects cs
JOIN terms t ON cs.term_id = t.term_id
JOIN classes c ON t.class_id = c.class_id
JOIN subjects s ON cs.subject_id = s.subject_id
LEFT JOIN subject_grades sg ON cs.class_subject_id = sg.class_subject_id
WHERE cs.teacher_id = 2
  AND sg.actual_average IS NOT NULL
  AND sg.actual_average > 0
GROUP BY c.class_id, c.name, c.grade_level, s.name
ORDER BY c.name;

-- ========== BONUS: CHECK IF QUERY RETURNS ANY DATA ==========
SELECT 'BONUS: Row counts' as step;
SELECT 
    (SELECT COUNT(*) FROM class_subjects WHERE teacher_id = 2) as class_subject_count,
    (SELECT COUNT(*) FROM subject_grades sg 
     JOIN class_subjects cs ON sg.class_subject_id = cs.class_subject_id 
     WHERE cs.teacher_id = 2) as total_grades,
    (SELECT COUNT(*) FROM subject_grades sg 
     JOIN class_subjects cs ON sg.class_subject_id = cs.class_subject_id 
     WHERE cs.teacher_id = 2 AND sg.actual_average IS NOT NULL AND sg.actual_average > 0) as valid_grades;
