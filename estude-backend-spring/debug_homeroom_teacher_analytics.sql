-- Debug: Check Homeroom Teacher Analytics Data
-- Verify all relationships for classId=1, teacherId=2

-- Step 1: Check Class basic info
SELECT 
    c.class_id,
    c.name as class_name,
    c.grade_level,
    c.homeroom_teacher_id,
    u.full_name as homeroom_teacher_name,
    COUNT(DISTINCT e.student_id) as enrolled_students
FROM classes c
LEFT JOIN teachers t ON c.homeroom_teacher_id = t.user_id
LEFT JOIN users u ON t.user_id = u.user_id
LEFT JOIN enrollments e ON c.class_id = e.class_id
WHERE c.class_id = 1
GROUP BY c.class_id, c.name, c.grade_level, c.homeroom_teacher_id, u.full_name;

-- Step 2: Check all subjects taught in this class
SELECT 
    cs.class_subject_id,
    s.name as subject_name,
    t2.user_id as teacher_id,
    u2.full_name as teacher_name,
    term.name as term_name,
    COUNT(sg.subject_grade_id) as total_grades
FROM class_subjects cs
JOIN terms term ON cs.term_id = term.term_id
JOIN subjects s ON cs.subject_id = s.subject_id
JOIN teachers t2 ON cs.teacher_id = t2.user_id
JOIN users u2 ON t2.user_id = u2.user_id
LEFT JOIN subject_grades sg ON cs.class_subject_id = sg.class_subject_id
WHERE term.class_id = 1
GROUP BY cs.class_subject_id, s.name, t2.user_id, u2.full_name, term.name
ORDER BY s.name;

-- Step 3: Check grades per subject with statistics
SELECT 
    s.name as subject_name,
    COUNT(DISTINCT sg.student_id) as graded_students,
    ROUND(AVG(sg.actual_average), 2) as avg_score,
    ROUND(AVG(CASE WHEN sg.actual_average >= 5.0 THEN 1.0 ELSE 0.0 END) * 100, 2) as pass_rate,
    ROUND(AVG(CASE WHEN sg.actual_average >= 9.0 THEN 1.0 ELSE 0.0 END) * 100, 2) as excellent_rate
FROM class_subjects cs
JOIN terms term ON cs.term_id = term.term_id
JOIN subjects s ON cs.subject_id = s.subject_id
LEFT JOIN subject_grades sg ON cs.class_subject_id = sg.class_subject_id 
    AND sg.actual_average IS NOT NULL 
    AND sg.actual_average > 0
WHERE term.class_id = 1
GROUP BY s.name
ORDER BY s.name;

-- Step 4: Check student overall averages (across all subjects)
SELECT 
    st.user_id as student_id,
    u.full_name as student_name,
    st.student_code,
    COUNT(DISTINCT cs.subject_id) as subjects_taken,
    COUNT(sg.subject_grade_id) as grades_received,
    ROUND(AVG(sg.actual_average), 2) as overall_avg
FROM enrollments e
JOIN students st ON e.student_id = st.user_id
JOIN users u ON st.user_id = u.user_id
JOIN classes c ON e.class_id = c.class_id
LEFT JOIN terms term ON term.class_id = c.class_id
LEFT JOIN class_subjects cs ON cs.term_id = term.term_id
LEFT JOIN subject_grades sg ON sg.class_subject_id = cs.class_subject_id 
    AND sg.student_id = st.user_id
    AND sg.actual_average IS NOT NULL
    AND sg.actual_average > 0
WHERE c.class_id = 1
GROUP BY st.user_id, u.full_name, st.student_code
ORDER BY overall_avg DESC NULLS LAST;

-- Step 5: Find Top 5 performers
SELECT 
    st.user_id as student_id,
    u.full_name as student_name,
    st.student_code,
    ROUND(AVG(sg.actual_average), 2) as overall_avg,
    COUNT(sg.subject_grade_id) as grades_count
FROM enrollments e
JOIN students st ON e.student_id = st.user_id
JOIN users u ON st.user_id = u.user_id
JOIN classes c ON e.class_id = c.class_id
LEFT JOIN terms term ON term.class_id = c.class_id
LEFT JOIN class_subjects cs ON cs.term_id = term.term_id
LEFT JOIN subject_grades sg ON sg.class_subject_id = cs.class_subject_id 
    AND sg.student_id = st.user_id
    AND sg.actual_average IS NOT NULL
    AND sg.actual_average > 0
WHERE c.class_id = 1
GROUP BY st.user_id, u.full_name, st.student_code
HAVING AVG(sg.actual_average) IS NOT NULL
ORDER BY overall_avg DESC
LIMIT 5;

-- Step 6: Find At-Risk students (avg < 5.0)
SELECT 
    st.user_id as student_id,
    u.full_name as student_name,
    st.student_code,
    ROUND(AVG(sg.actual_average), 2) as overall_avg,
    COUNT(sg.subject_grade_id) as grades_count
FROM enrollments e
JOIN students st ON e.student_id = st.user_id
JOIN users u ON st.user_id = u.user_id
JOIN classes c ON e.class_id = c.class_id
LEFT JOIN terms term ON term.class_id = c.class_id
LEFT JOIN class_subjects cs ON cs.term_id = term.term_id
LEFT JOIN subject_grades sg ON sg.class_subject_id = cs.class_subject_id 
    AND sg.student_id = st.user_id
    AND sg.actual_average IS NOT NULL
    AND sg.actual_average > 0
WHERE c.class_id = 1
GROUP BY st.user_id, u.full_name, st.student_code
HAVING AVG(sg.actual_average) < 5.0
ORDER BY overall_avg ASC;

-- Step 7: Overall diagnosis
SELECT 
    '✅ Class Info' as check_type,
    CASE 
        WHEN COUNT(*) > 0 THEN 'Class exists'
        ELSE '❌ Class not found'
    END as status
FROM classes WHERE class_id = 1
UNION ALL
SELECT 
    '✅ Students Enrolled',
    CASE 
        WHEN COUNT(*) > 0 THEN CONCAT(COUNT(*), ' students enrolled')
        ELSE '❌ No students enrolled'
    END
FROM enrollments WHERE class_id = 1
UNION ALL
SELECT 
    '✅ Subjects Taught',
    CASE 
        WHEN COUNT(*) > 0 THEN CONCAT(COUNT(DISTINCT cs.subject_id), ' subjects')
        ELSE '❌ No subjects assigned'
    END
FROM class_subjects cs
JOIN terms t ON cs.term_id = t.term_id
WHERE t.class_id = 1
UNION ALL
SELECT 
    '✅ Grades Recorded',
    CASE 
        WHEN COUNT(*) > 0 THEN CONCAT(COUNT(*), ' grades recorded')
        ELSE '⚠️ No grades yet'
    END
FROM subject_grades sg
JOIN class_subjects cs ON sg.class_subject_id = cs.class_subject_id
JOIN terms t ON cs.term_id = t.term_id
WHERE t.class_id = 1 AND sg.actual_average IS NOT NULL AND sg.actual_average > 0;

-- Expected Results:
-- Query 1: Should show class name, grade level, homeroom teacher, student count
-- Query 2: Should show all subjects with teachers (e.g., Toán, Vật Lý, Hóa, etc.)
-- Query 3: Should show performance metrics per subject
-- Query 4: Should show all students with their overall averages
-- Query 5: Should show top 5 students by average
-- Query 6: Should show students with avg < 5.0
-- Query 7: Should show diagnosis of data completeness
