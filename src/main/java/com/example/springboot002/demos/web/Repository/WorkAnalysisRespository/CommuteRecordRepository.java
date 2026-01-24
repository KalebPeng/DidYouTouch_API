package com.example.springboot002.demos.web.Repository.WorkAnalysisRespository;

import com.example.springboot002.demos.web.Entity.WorkAnalysisEntity.CommuteRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommuteRecordRepository extends JpaRepository<CommuteRecord, String> {

    List<CommuteRecord> findByWorkAnalysisIdOrderByRecordDateDesc(String workAnalysisId);

    @Query("SELECT c FROM CommuteRecord c WHERE c.workAnalysisId = :workAnalysisId " +
            "AND c.recordDate >= :startDate AND c.recordDate <= :endDate")
    List<CommuteRecord> findByWorkAnalysisIdAndDateRange(
            @Param("workAnalysisId") String workAnalysisId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT AVG(c.actualDurationMinutes) FROM CommuteRecord c " +
            "WHERE c.workAnalysisId = :workAnalysisId AND c.commuteType = :commuteType " +
            "AND c.actualDurationMinutes IS NOT NULL")
    Double getAverageDurationByType(@Param("workAnalysisId") String workAnalysisId,
                                    @Param("commuteType") String commuteType);

    @Query("SELECT c FROM CommuteRecord c WHERE c.workAnalysisId = :workAnalysisId " +
            "AND c.isPeakHour = true ORDER BY c.recordDate DESC")
    List<CommuteRecord> findPeakHourRecords(@Param("workAnalysisId") String workAnalysisId);
}
