package com.example.springboot002.demos.web.DTO.Response.WorkAnalysisResponse;

import lombok.Data;

//统计响应DTO
@Data
public class CommuteStatisticsResponse {
    private Integer averageMorningDuration;
    private Integer averageEveningDuration;
    private Integer totalRecords;
}
