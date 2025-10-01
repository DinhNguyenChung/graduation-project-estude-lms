package org.example.estudebackendspring.dto;

import lombok.Data;
import org.example.estudebackendspring.enums.ScheduleStatus;
import org.example.estudebackendspring.enums.ScheduleType;

import java.util.Date;

@Data
public class ScheduleDTO {
    private Long scheduleId;
    private Integer week;
    private String details;
    private Date date;
    private Integer startPeriod;
    private Integer endPeriod;
    private String room;
    private ScheduleStatus status;
    private ScheduleType type;
    private TermDTO term;
    private ClassSubjectDTO classSubject;

    @Data
    public static class TermDTO {
        private Long termId;
        private String name;
        private Date beginDate;
        private Date endDate;
    }

    @Data
    public static class ClassSubjectDTO {
        private Long classSubjectId;
        private Long subjectId;
        private String subjectName;
        private Long classId;
        private String className;
        private Long teacherId;
        private String teacherName;
    }
}
