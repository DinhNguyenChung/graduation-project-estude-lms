-- Debug script to check Teacher ID = 2 data

-- 1. Check if teacher exists
SELECT * FROM users WHERE user_id = 2;
SELECT * FROM teachers WHERE user_id = 2;

-- 2. Check class_subjects assigned to teacher WITH TERM AND CLASS INFO
SELECT 
    cs.class_subject_id,
    cs.teacher_id,
    cs.term_id,
    s.name as subject_name,
    t.name as term_name,
    t.class_id,
    c.name as class_name,
    c.grade_level
FROM class_subjects cs
LEFT JOIN subjects s ON cs.subject_id = s.subject_id
LEFT JOIN terms t ON cs.term_id = t.term_id
LEFT JOIN classes c ON t.class_id = c.class_id
WHERE cs.teacher_id = 2;

-- 2a. Check if any term_id is NULL
SELECT 
    cs.class_subject_id,
    cs.teacher_id,
    cs.term_id,
    CASE WHEN cs.term_id IS NULL THEN 'NULL TERM!' ELSE 'OK' END as term_status
FROM class_subjects cs
WHERE cs.teacher_id = 2;

-- 2b. Check if any term.class_id is NULL
SELECT 
    cs.class_subject_id,
    t.term_id,
    t.class_id,
    CASE WHEN t.class_id IS NULL THEN 'NULL CLASS!' ELSE 'OK' END as class_status
FROM class_subjects cs
JOIN terms t ON cs.term_id = t.term_id
WHERE cs.teacher_id = 2;

-- 3. Check subject_grades for teacher's class_subjects
SELECT 
    sg.subject_grade_id,
    sg.actual_average,
    st.full_name as student_name,
    s.name as subject_name,
    c.name as class_name
FROM subject_grades sg
JOIN students st ON sg.student_id = st.user_id
JOIN class_subjects cs ON sg.class_subject_id = cs.class_subject_id
JOIN subjects s ON cs.subject_id = s.subject_id
JOIN terms t ON cs.term_id = t.term_id
JOIN classes c ON t.class_id = c.class_id
WHERE cs.teacher_id = 2
ORDER BY c.name, s.name, st.full_name;

-- 4. Count statistics for teacher
SELECT 
    COUNT(DISTINCT cs.class_subject_id) as class_subject_count,
    COUNT(DISTINCT c.class_id) as unique_class_count,
    COUNT(DISTINCT s.subject_id) as subject_count,
    COUNT(DISTINCT sg.student_id) as unique_student_count,
    AVG(sg.actual_average) as overall_avg_score,
    SUM(CASE WHEN sg.actual_average >= 5.0 THEN 1 ELSE 0 END) as pass_count,
    SUM(CASE WHEN sg.actual_average >= 9.0 THEN 1 ELSE 0 END) as excellent_count,
    COUNT(sg.subject_grade_id) as total_grades
FROM class_subjects cs
LEFT JOIN subject_grades sg ON cs.class_subject_id = sg.class_subject_id
LEFT JOIN subjects s ON cs.subject_id = s.subject_id
LEFT JOIN terms t ON cs.term_id = t.term_id
LEFT JOIN classes c ON t.class_id = c.class_id
WHERE cs.teacher_id = 2;

-- 5. Check if there are any NULL values causing issues
SELECT 
    cs.class_subject_id,
    cs.teacher_id,
    cs.subject_id,
    cs.term_id,
    t.class_id,
    COUNT(sg.subject_grade_id) as grade_count
FROM class_subjects cs
LEFT JOIN terms t ON cs.term_id = t.term_id
LEFT JOIN subject_grades sg ON cs.class_subject_id = sg.class_subject_id
WHERE cs.teacher_id = 2
GROUP BY cs.class_subject_id, cs.teacher_id, cs.subject_id, cs.term_id, t.class_id;
