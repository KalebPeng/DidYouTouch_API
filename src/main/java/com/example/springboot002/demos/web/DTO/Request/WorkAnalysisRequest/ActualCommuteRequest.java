package com.example.springboot002.demos.web.DTO.Request.WorkAnalysisRequest;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

// 实际通勤记录请求
@Data
public class ActualCommuteRequest {
    @NotBlank
    private String workAnalysisId;

    @NotNull
    @Min(1)
    private Integer actualDurationMinutes;

    @NotNull
    private String commuteType; // "morning" or "evening"

    private String commuteModes; // 实际使用的通勤方式组合
}
