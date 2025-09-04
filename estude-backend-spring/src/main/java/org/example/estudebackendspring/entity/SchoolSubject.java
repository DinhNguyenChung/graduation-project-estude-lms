package org.example.estudebackendspring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "school_subjects")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SchoolSubject {
    @Id
    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;

    @Id
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;
}
