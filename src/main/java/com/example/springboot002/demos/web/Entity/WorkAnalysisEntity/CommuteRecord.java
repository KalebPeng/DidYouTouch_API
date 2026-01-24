package com.example.springboot002.demos.web.Entity.WorkAnalysisEntity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// 3. 通勤记录实体 - CommuteRecord.java
@Data
@Entity
@Table(name = "commute_record")
public class CommuteRecord {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @Column(name = "work_analysis_id", nullable = false)
    private String workAnalysisId;

    @Column(name = "commute_type", length = 20)
    private String commuteType; // "morning" or "evening"

    @Column(name = "distance_meters")
    private Integer distanceMeters;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "api_duration_minutes")
    private Integer apiDurationMinutes;

    @Column(name = "actual_duration_minutes")
    private Integer actualDurationMinutes;

    @Column(name = "peak_correction_factor", precision = 3, scale = 2)
    private BigDecimal peakCorrectionFactor;

    @Column(name = "commute_modes", length = 200)
    private String commuteModes; // JSON格式存储多种通勤方式

    @Column(name = "record_date")
    private LocalDateTime recordDate;

    @Column(name = "is_peak_hour")
    private Boolean isPeakHour;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (recordDate == null) {
            recordDate = LocalDateTime.now();
        }
    }
}
