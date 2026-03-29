package org.example.learnlink.modules.user.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a subject/course that students can study.
 * Used for matching students with similar interests.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSubject {
    
    @Id
    @GeneratedValue
    private Long id;
    
    private String name;

    @ManyToMany(mappedBy = "subjects")
    @Builder.Default
    private List<UserProfile> userProfiles = new ArrayList<>();

    public StudentSubject(String name) {
        this.name = name;
    }
}
