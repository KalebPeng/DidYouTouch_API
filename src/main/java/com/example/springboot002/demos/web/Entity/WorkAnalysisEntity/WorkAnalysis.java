package com.example.springboot002.demos.web.Entity.WorkAnalysisEntity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "work_analysis")
public class WorkAnalysis {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "monthly_salary", precision = 10, scale = 2)
    private BigDecimal monthlySalary;

    @Column(name = "annual_salary", precision = 12, scale = 2)
    private BigDecimal annualSalary;

    @Column(name = "work_days_per_month", columnDefinition = "INT DEFAULT 22")
    private Integer workDaysPerMonth = 22;

    @Column(name = "home_address", length = 500)
    private String homeAddress;

    @Column(name = "company_address", length = 500)
    private String companyAddress;

    @Column(name = "home_longitude")
    private Double homeLongitude;

    @Column(name = "home_latitude")
    private Double homeLatitude;

    @Column(name = "company_longitude")
    private Double companyLongitude;

    @Column(name = "company_latitude")
    private Double companyLatitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "commute_mode", length = 50)
    private CommuteMode commuteMode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
