-- Based on your data: teacher_id=2 has class_subject_id 1 and 7

-- Check if SubjectGrades exist for these ClassSubjects
SELECT 
    cs.class_subject_id,
    s.name as subject_name,
    COUNT(sg.subject_grade_id) as total_grades,
    COUNT(CASE WHEN sg.actual_average IS NOT NULL AND sg.actual_average > 0 THEN 1 END) as valid_grades,
    AVG(CASE WHEN sg.actual_average IS NOT NULL AND sg.actual_average > 0 THEN sg.actual_average END) as avg_score
FROM class_subjects cs
LEFT JOIN subjects s ON cs.subject_id = s.subject_id
LEFT JOIN subject_grades sg ON cs.class_subject_id = sg.class_subject_id
WHERE cs.class_subject_id IN (1, 7)
GROUP BY cs.class_subject_id, s.name;

-- Check individual grades
SELECT 
    cs.class_subject_id,
    sg.subject_grade_id,
    sg.student_id,
    sg.actual_average,
    CASE 
        WHEN sg.actual_average IS NULL THEN '❌ NULL'
        WHEN sg.actual_average = 0 THEN '❌ ZERO'
        WHEN sg.actual_average > 0 THEN '✅ VALID'
    END as status
FROM class_subjects cs
LEFT JOIN subject_grades sg ON cs.class_subject_id = sg.class_subject_id
WHERE cs.class_subject_id IN (1, 7)
ORDER BY cs.class_subject_id, sg.actual_average DESC
LIMIT 20;

-- Check if ANY valid grades exist
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '❌ NO GRADES AT ALL'
        WHEN COUNT(CASE WHEN sg.actual_average IS NOT NULL AND sg.actual_average > 0 THEN 1 END) = 0 
            THEN '❌ ALL GRADES ARE NULL OR ZERO'
        ELSE '✅ VALID GRADES EXIST'
    END as diagnosis,
    COUNT(*) as total_grade_records,
    COUNT(CASE WHEN sg.actual_average IS NOT NULL AND sg.actual_average > 0 THEN 1 END) as valid_grade_count
FROM subject_grades sg
WHERE sg.class_subject_id IN (1, 7);
