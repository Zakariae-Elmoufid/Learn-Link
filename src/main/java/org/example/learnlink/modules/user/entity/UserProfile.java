package org.example.learnlink.modules.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private long userId;

    @Column(length = 500)
    private String bio;


    @ManyToMany
    @JoinTable(
            name = "user_profile_subject",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private List<StudentSubject> subjects = new ArrayList<>();

    private String firstName;
    private String lastName;

    private String profilePictureUrl;


    @Enumerated(EnumType.STRING)
    private AcademicLevel academicLevel;



}
