package com.example.springboot002.demos.web.Repository.WorkAnalysisRespository;

import com.example.springboot002.demos.web.Entity.WorkAnalysisEntity.WorkAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkAnalysisRepository extends JpaRepository<WorkAnalysis, String> {

    Optional<WorkAnalysis> findByUserId(String userId);

    List<WorkAnalysis> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query("SELECT w FROM WorkAnalysis w WHERE w.userId = :userId AND w.createdAt >= :startDate")
    List<WorkAnalysis> findRecentByUserId(@Param("userId") String userId,
                                          @Param("startDate") LocalDateTime startDate);
}
