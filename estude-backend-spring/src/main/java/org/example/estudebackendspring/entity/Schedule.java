package org.example.estudebackendspring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.estudebackendspring.enums.ScheduleStatus;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "schedules")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;
    
    private String week;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @Temporal(TemporalType.DATE)
    private Date date;
    
    private Integer startPeriod;    private Integer endPeriod;
    private String room;
    
    @Enumerated(EnumType.STRING)
    private ScheduleStatus status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private Clazz clazz;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_subject_id")
    private ClassSubject classSubject;
    
    /**
     * Helper method to check if this schedule belongs to a student with the given code
     * @param studentCode The student code to check
     * @return true if this schedule belongs to a student with the given code, false otherwise
     */
    @Transient
    public boolean isForStudent(String studentCode) {
        if (clazz == null || clazz.getEnrollments() == null) {
            return false;
        }
        
        return clazz.getEnrollments().stream()
                .anyMatch(enrollment -> 
                    enrollment.getStudent() != null && 
                    studentCode.equals(enrollment.getStudent().getStudentCode())
                );
    }
}
