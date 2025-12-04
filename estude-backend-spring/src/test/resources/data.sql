-- H2 Test Database Sample Data
-- Uses BCrypt hashed passwords for testing
-- IMPORTANT: This must follow the Hibernate JOINED inheritance pattern!
-- Password hash: $2a$10$9.jhKlXgn.WVVVF6yjR5Ou1kU3K1D3eCQv0tFoGzL5K5M3Q7e
-- Passwords: admin123, teacher123, 123456

-- Step 1: Insert School
INSERT INTO school (school_id, school_name, address, phone, email, created_at) 
VALUES (1, 'Test School', '123 Test Street', '0123456789', 'school@test.edu', CURRENT_TIMESTAMP);

-- Step 2: Insert into USERS table (parent table for JOINED inheritance)
-- Admin user
INSERT INTO users (user_id, email, password, role, full_name, number_phone, dob, avatar_path, school_id, dtype) 
VALUES (1, 'admin01@test.edu', '$2a$10$9.jhKlXgn.WVVVF6yjR5Ou1kU3K1D3eCQv0tFoGzL5K5M3Q7e', 'ADMIN', 'Admin Test', '0123456789', '1990-01-01', NULL, 1, 'Admin');

-- Teacher user
INSERT INTO users (user_id, email, password, role, full_name, number_phone, dob, avatar_path, school_id, dtype) 
VALUES (2, 'teacher01@test.edu', '$2a$10$9.jhKlXgn.WVVVF6yjR5Ou1kU3K1D3eCQv0tFoGzL5K5M3Q7e', 'TEACHER', 'Giáo Viên Test', '0123456790', '1990-02-01', NULL, 1, 'Teacher');

-- Student user
INSERT INTO users (user_id, email, password, role, full_name, number_phone, dob, avatar_path, school_id, dtype) 
VALUES (3, 'student1@test.edu', '$2a$10$9.jhKlXgn.WVVVF6yjR5Ou1kU3K1D3eCQv0tFoGzL5K5M3Q7e', 'STUDENT', 'Học Sinh Test', '0123456791', '2005-03-01', NULL, 1, 'Student');

-- Step 3: Insert into child tables (ADMINS, TEACHERS, STUDENTS)
-- Admin
INSERT INTO admins (user_id, admin_code)
VALUES (1, 'admin01');

-- Teacher
INSERT INTO teachers (user_id, teacher_code, is_admin, is_homeroom_teacher, created_at)
VALUES (2, 'teacher01', FALSE, FALSE, CURRENT_TIMESTAMP);

-- Student
INSERT INTO students (user_id, student_code, enrollment_date, completion_date)
VALUES (3, 'student1', CURRENT_DATE, NULL);

-- Step 4: Insert Class
INSERT INTO clazz (class_id, class_name, school_id, homeroom_teacher_id, created_at)
VALUES (1, '10A1', 1, 2, CURRENT_TIMESTAMP);

-- Step 5: Link Student to Class
UPDATE students SET class_id = 1 WHERE user_id = 3;

-- Step 6: Insert Subjects
INSERT INTO subject (subject_id, subject_name, school_id, description, created_at)
VALUES (1, 'Toán', 1, 'Toán học', CURRENT_TIMESTAMP);

INSERT INTO subject (subject_id, subject_name, school_id, description, created_at)
VALUES (2, 'Vật Lý', 1, 'Vật lý', CURRENT_TIMESTAMP);

-- Step 7: Insert Topics
INSERT INTO topic (topic_id, topic_name, subject_id, description, created_at)
VALUES (1, 'Hàm số', 1, 'Hàm số cơ bản', CURRENT_TIMESTAMP);

INSERT INTO topic (topic_id, topic_name, subject_id, description, created_at)
VALUES (2, 'Phương trình bậc nhất', 1, 'Phương trình bậc nhất một ẩn', CURRENT_TIMESTAMP);

INSERT INTO topic (topic_id, topic_name, subject_id, description, created_at)
VALUES (3, 'Chuyển động', 2, 'Chuyển động trong vật lý', CURRENT_TIMESTAMP);

-- Step 8: Insert Questions (must reference teacher.teacher_id = 2 for user_id 2)
-- Need to insert into question table with created_by = teacher_id, not user_id
INSERT INTO question (question_id, content, difficulty, correct_answer, explanation, topic_id, created_by, created_at)
VALUES (1, 'Cho hàm số y = 2x + 3. Tìm y khi x = 1?', 'MEDIUM', 'A', 'y = 2(1) + 3 = 5', 1, 2, CURRENT_TIMESTAMP);

INSERT INTO question (question_id, content, difficulty, correct_answer, explanation, topic_id, created_by, created_at)
VALUES (2, '2 + 2 bằng bao nhiêu?', 'EASY', 'B', 'Phép cộng cơ bản: 2 + 2 = 4', 1, 2, CURRENT_TIMESTAMP);

INSERT INTO question (question_id, content, difficulty, correct_answer, explanation, topic_id, created_by, created_at)
VALUES (3, 'Phương trình bậc nhất là gì?', 'MEDIUM', 'A', 'Phương trình có ẩn số mũ 1', 2, 2, CURRENT_TIMESTAMP);

INSERT INTO question (question_id, content, difficulty, correct_answer, explanation, topic_id, created_by, created_at)
VALUES (4, 'Giải x: 2x = 4', 'EASY', 'C', 'x = 4/2 = 2', 2, 2, CURRENT_TIMESTAMP);

INSERT INTO question (question_id, content, difficulty, correct_answer, explanation, topic_id, created_by, created_at)
VALUES (5, '3 × 5 bằng bao nhiêu?', 'EASY', 'D', 'Phép nhân: 3 × 5 = 15', 1, 2, CURRENT_TIMESTAMP);

-- Step 9: Insert Test
INSERT INTO test (test_id, test_name, class_id, duration_minutes, total_questions, created_by, created_at)
VALUES (1, 'Kiểm tra Toán - Tuần 1', 1, 45, 5, 2, CURRENT_TIMESTAMP);

-- Step 10: Insert Test Questions
INSERT INTO test_question (test_id, question_id) VALUES (1, 1);
INSERT INTO test_question (test_id, question_id) VALUES (1, 2);
INSERT INTO test_question (test_id, question_id) VALUES (1, 3);
INSERT INTO test_question (test_id, question_id) VALUES (1, 4);
INSERT INTO test_question (test_id, question_id) VALUES (1, 5);

-- Step 11: Insert Sample Submission
-- student_id should reference students.user_id = 3
INSERT INTO submission (submission_id, test_id, student_id, score, status, submitted_at)
VALUES (1, 1, 3, 80.00, 'SUBMITTED', CURRENT_TIMESTAMP);

-- Step 12: Insert Sample Attendance
INSERT INTO attendance_record (attendance_id, class_id, student_id, attendance_date, status)
VALUES (1, 1, 3, CURRENT_DATE, 'PRESENT');

-- Step 13: Insert Sample AI Analysis
INSERT INTO ai_analysis_request (analysis_id, submission_id, weak_topics, created_at)
VALUES (1, 1, 'Hàm số, Phương trình bậc hai', CURRENT_TIMESTAMP);

-- Step 14: Insert Sample Roadmap
INSERT INTO ai_roadmap (roadmap_id, student_id, roadmap_data, created_at)
VALUES (1, 3, '{"week": 1, "topics": ["Hàm số", "Phương trình"]}', CURRENT_TIMESTAMP);
