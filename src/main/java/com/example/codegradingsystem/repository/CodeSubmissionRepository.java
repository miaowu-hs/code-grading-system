package com.example.codegradingsystem.repository;

import com.example.codegradingsystem.entity.CodeSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeSubmissionRepository extends JpaRepository<CodeSubmission, Long> {
}
