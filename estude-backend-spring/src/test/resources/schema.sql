-- H2 Test Database Schema (Minimal for ApiTestSuite)
-- This schema provides the basic structure needed for test execution

CREATE TABLE IF NOT EXISTS school (
    school_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    school_name VARCHAR(255),
    address VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "user" (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50),
    full_name VARCHAR(255),
    avatar_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin (
    admin_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    admin_code VARCHAR(50) UNIQUE,
    school_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES "user"(user_id),
    FOREIGN KEY (school_id) REFERENCES school(school_id)
);

CREATE TABLE IF NOT EXISTS teacher (
    teacher_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    teacher_code VARCHAR(50) UNIQUE,
    school_id BIGINT,
    is_admin BOOLEAN DEFAULT FALSE,
    is_homeroom_teacher BOOLEAN DEFAULT FALSE,
    department VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "user"(user_id),
    FOREIGN KEY (school_id) REFERENCES school(school_id)
);

CREATE TABLE IF NOT EXISTS student (
    student_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    student_code VARCHAR(50) UNIQUE,
    school_id BIGINT,
    class_id BIGINT,
    enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "user"(user_id),
    FOREIGN KEY (school_id) REFERENCES school(school_id)
);

CREATE TABLE IF NOT EXISTS clazz (
    class_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_name VARCHAR(255),
    school_id BIGINT,
    homeroom_teacher_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (school_id) REFERENCES school(school_id),
    FOREIGN KEY (homeroom_teacher_id) REFERENCES teacher(teacher_id)
);

CREATE TABLE IF NOT EXISTS subject (
    subject_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    subject_name VARCHAR(255),
    school_id BIGINT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (school_id) REFERENCES school(school_id)
);

CREATE TABLE IF NOT EXISTS topic (
    topic_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    topic_name VARCHAR(255),
    subject_id BIGINT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subject(subject_id)
);

CREATE TABLE IF NOT EXISTS question (
    question_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT,
    difficulty VARCHAR(50),
    correct_answer VARCHAR(255),
    explanation TEXT,
    topic_id BIGINT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (topic_id) REFERENCES topic(topic_id),
    FOREIGN KEY (created_by) REFERENCES teacher(teacher_id)
);

CREATE TABLE IF NOT EXISTS test (
    test_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    test_name VARCHAR(255),
    class_id BIGINT,
    duration_minutes INT,
    total_questions INT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES clazz(class_id),
    FOREIGN KEY (created_by) REFERENCES teacher(teacher_id)
);

CREATE TABLE IF NOT EXISTS test_question (
    test_question_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    test_id BIGINT,
    question_id BIGINT,
    FOREIGN KEY (test_id) REFERENCES test(test_id),
    FOREIGN KEY (question_id) REFERENCES question(question_id)
);

CREATE TABLE IF NOT EXISTS submission (
    submission_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    test_id BIGINT,
    student_id BIGINT,
    submitted_at TIMESTAMP,
    score DECIMAL(5,2),
    status VARCHAR(50),
    FOREIGN KEY (test_id) REFERENCES test(test_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id)
);

CREATE TABLE IF NOT EXISTS attendance (
    attendance_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_id BIGINT,
    student_id BIGINT,
    attendance_date DATE,
    status VARCHAR(50),
    FOREIGN KEY (class_id) REFERENCES clazz(class_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id)
);

CREATE TABLE IF NOT EXISTS assessment (
    assessment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT,
    teacher_id BIGINT,
    feedback TEXT,
    assessment_date TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES submission(submission_id),
    FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id)
);

CREATE TABLE IF NOT EXISTS ai_analysis (
    analysis_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT,
    analysis_result TEXT,
    weak_topics VARCHAR(255),
    created_at TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES submission(submission_id)
);

CREATE TABLE IF NOT EXISTS ai_roadmap (
    roadmap_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT,
    roadmap_data TEXT,
    created_at TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES student(student_id)
);
