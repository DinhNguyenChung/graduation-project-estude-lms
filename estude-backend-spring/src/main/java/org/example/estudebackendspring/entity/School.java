package org.example.estudebackendspring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "schools")
public class School {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long schoolId;
    
    @Column(unique = true, nullable = false)
    private String schoolCode;
    
    @Column(nullable = false)
    private String schoolName;
    
    private String address;
    private String contactEmail;
    private String contactPhone;
    
    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users;
}
