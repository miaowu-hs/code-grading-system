package com.example.codegradingsystem.repository;

import com.example.codegradingsystem.entity.CodeSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 代码提交记录仓库
 */
@Repository
public interface CodeSubmissionRepository extends JpaRepository<CodeSubmission, Long> {
    
    /**
     * 根据用户ID查询提交记录
     */
    List<CodeSubmission> findByUserIdOrderBySubmitTimeDesc(Long userId);
    
    /**
     * 根据作业ID查询提交记录
     */
    List<CodeSubmission> findByAssignmentIdOrderBySubmitTimeDesc(Long assignmentId);
}