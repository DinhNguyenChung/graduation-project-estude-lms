package org.example.estudebackendspring.enums;

public enum NotificationTargetType {
    SYSTEM,    // toàn hệ thống
    SCHOOL,        // tất cả user trong school (targetId = schoolId)
    CLASS,         // tất cả user trong class (targetId = classId)
    CLASS_SUBJECT, // tất cả user in classSubject (targetId = classSubjectId)

}
