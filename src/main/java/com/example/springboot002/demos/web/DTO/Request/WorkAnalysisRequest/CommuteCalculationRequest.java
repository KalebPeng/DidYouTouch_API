package com.example.springboot002.demos.web.DTO.Request.WorkAnalysisRequest;

import lombok.Data;

import javax.validation.constraints.NotBlank;

// 通勤时间计算请求
@Data
public class CommuteCalculationRequest {
    @NotBlank
    private String workAnalysisId;

    private String origin; // 起点地址或坐标
    private String destination; // 终点地址或坐标
    private String mode; // 通勤方式
    private Boolean isPeakHour; // 是否高峰期
}
