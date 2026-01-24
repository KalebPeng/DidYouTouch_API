package com.example.springboot002.demos.web.DTO.Response.WorkAnalysisResponse;

import lombok.Data;

@Data
public class CommuteInfoResponse {
    private Integer distanceMeters;
    private String distanceText; // "5.2公里"
    private Integer durationMinutes;
    private String durationText; // "35分钟"
    private Integer morningPeakDuration;
    private Integer eveningPeakDuration;
    private String[] commuteModes;
}