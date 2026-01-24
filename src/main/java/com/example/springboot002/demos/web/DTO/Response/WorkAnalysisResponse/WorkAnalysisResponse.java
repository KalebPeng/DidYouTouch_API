package com.example.springboot002.demos.web.DTO.Response.WorkAnalysisResponse;

import lombok.Data;

import java.math.BigDecimal;

// DTO 响应类
@Data
public class WorkAnalysisResponse {
    private String id;
    private String userId;
    private BigDecimal monthlySalary;
    private BigDecimal annualSalary;
    private Integer workDaysPerMonth;
    private String homeAddress;
    private String companyAddress;
    private String commuteMode;
    private CommuteInfoResponse commuteInfo;
}