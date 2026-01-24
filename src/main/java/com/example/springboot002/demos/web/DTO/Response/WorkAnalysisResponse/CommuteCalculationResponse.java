package com.example.springboot002.demos.web.DTO.Response.WorkAnalysisResponse;

import lombok.Data;

// 通勤时间计算响应
@Data
public class CommuteCalculationResponse {
    private Integer distanceMeters;
    private String distanceText;
    private Integer durationMinutes;
    private String durationText;
    private Integer correctedDuration; // 修正后的时长
    private String[] routes; // 路线信息
    private MapApiResponse apiResponse;
}
