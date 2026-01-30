package org.example.learnlink.modules.user.repository;

import org.example.learnlink.modules.user.entity.StudentSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentSubjectRepository extends JpaRepository< StudentSubject , Long> {

}
