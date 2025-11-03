-- Debug: Check Enrollment Data for Class 10A1
-- Verify students are properly enrolled in classes

-- Step 1: Check enrollments for class_id=1 (10A1)
SELECT 
    e.enrollment_id,
    e.class_id,
    c.name as class_name,
    e.student_id,
    u.full_name as student_name,
    s.student_code,
    e.date_joined
FROM enrollments e
JOIN classes c ON e.class_id = c.class_id
JOIN students s ON e.student_id = s.user_id
JOIN users u ON s.user_id = u.user_id
WHERE e.class_id = 1
ORDER BY e.enrollment_id;

-- Step 2: Count students per class
SELECT 
    c.class_id,
    c.name as class_name,
    c.grade_level,
    COUNT(e.enrollment_id) as total_enrolled_students
FROM classes c
LEFT JOIN enrollments e ON c.class_id = e.class_id
WHERE c.class_id = 1
GROUP BY c.class_id, c.name, c.grade_level;

-- Step 3: Check all classes for teacher_id=2
SELECT DISTINCT
    c.class_id,
    c.name as class_name,
    c.grade_level,
    COUNT(DISTINCT e.student_id) as enrolled_students,
    COUNT(DISTINCT sg.student_id) as graded_students
FROM class_subjects cs
JOIN terms t ON cs.term_id = t.term_id
JOIN classes c ON t.class_id = c.class_id
LEFT JOIN enrollments e ON c.class_id = e.class_id
LEFT JOIN subject_grades sg ON cs.class_subject_id = sg.class_subject_id
WHERE cs.teacher_id = 2
GROUP BY c.class_id, c.name, c.grade_level
ORDER BY c.class_id;

-- Step 4: Detailed view - Which students are enrolled vs graded
SELECT 
    c.name as class_name,
    u.full_name as student_name,
    s.student_code,
    e.enrollment_id as enrolled,
    sg.subject_grade_id as has_grade,
    sg.actual_average as grade_value
FROM classes c
JOIN enrollments e ON c.class_id = e.class_id
JOIN students s ON e.student_id = s.user_id
JOIN users u ON s.user_id = u.user_id
LEFT JOIN class_subjects cs ON cs.term_id IN (
    SELECT term_id FROM terms WHERE class_id = c.class_id
) AND cs.teacher_id = 2
LEFT JOIN subject_grades sg ON sg.student_id = s.user_id AND sg.class_subject_id = cs.class_subject_id
WHERE c.class_id = 1
ORDER BY u.full_name;

-- Expected Results:
-- Query 1: Should show list of all students enrolled in 10A1
-- Query 2: Should show total count (e.g., 35 students)
-- Query 3: Should show enrolled_students > 0, graded_students might be 0
-- Query 4: Should show enrolled=true for students, has_grade might be NULL
