package org.example.estudebackendspring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.example.estudebackendspring.enums.GradeLevel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subjects")
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subjectId;
    
    @Column(nullable = false )
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
    
    // Note: Grade level and volume are now managed at Topic level
    // This allows one subject (e.g., "Toán") to have topics for multiple grades and volumes
    // Example: Subject "Toán" → Topics for Grade 10/11/12, Volume 1/2
    
    // Optional: Keep this if you want to allow subjects to be assigned to specific schools
    // Comment out or remove if subjects should be global (shared across all schools)
    /*
    @ManyToMany
    @JoinTable(
            name = "school_subjects",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "school_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Set<School> schools = new HashSet<>();
    */
    
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ClassSubject> classSubjects;
}
