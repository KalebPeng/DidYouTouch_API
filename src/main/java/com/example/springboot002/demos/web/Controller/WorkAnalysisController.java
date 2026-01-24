package com.example.springboot002.demos.web.Controller;

import com.example.springboot002.demos.web.DTO.Request.WorkAnalysisRequest.ActualCommuteRequest;
import com.example.springboot002.demos.web.DTO.Request.WorkAnalysisRequest.CommuteCalculationRequest;
import com.example.springboot002.demos.web.DTO.Request.WorkAnalysisRequest.WorkAnalysisRequest;
import com.example.springboot002.demos.web.DTO.Response.WorkAnalysisResponse.CommuteCalculationResponse;
import com.example.springboot002.demos.web.DTO.Response.WorkAnalysisResponse.CommuteStatisticsResponse;
import com.example.springboot002.demos.web.DTO.Response.WorkAnalysisResponse.WorkAnalysisResponse;
import com.example.springboot002.demos.web.Service.WorkAnalysisService.WorkAnalysisService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/work-analysis")
@RequiredArgsConstructor
public class WorkAnalysisController {

    private final WorkAnalysisService workAnalysisService;

    /**
     * 创建工作分析记录
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WorkAnalysisResponse>> createWorkAnalysis(
            @Valid @RequestBody WorkAnalysisRequest request) {
        WorkAnalysisResponse response = workAnalysisService.createWorkAnalysis(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 更新工作分析记录
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkAnalysisResponse>> updateWorkAnalysis(
            @PathVariable String id,
            @Valid @RequestBody WorkAnalysisRequest request) {
        WorkAnalysisResponse response = workAnalysisService.updateWorkAnalysis(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取工作分析记录
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkAnalysisResponse>> getWorkAnalysis(
            @PathVariable String id) {
        WorkAnalysisResponse response = workAnalysisService.getWorkAnalysis(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 根据用户ID获取工作分析记录
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<WorkAnalysisResponse>> getWorkAnalysisByUserId(
            @PathVariable String userId) {
        WorkAnalysisResponse response = workAnalysisService.getWorkAnalysisByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 计算通勤时间
     */
    @PostMapping("/commute/calculate")
    public ResponseEntity<ApiResponse<CommuteCalculationResponse>> calculateCommuteTime(
            @Valid @RequestBody CommuteCalculationRequest request) {
        CommuteCalculationResponse response = workAnalysisService.calculateCommuteTime(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 记录实际通勤时间
     */
    @PostMapping("/commute/record")
    public ResponseEntity<ApiResponse<Void>> recordActualCommute(
            @Valid @RequestBody ActualCommuteRequest request) {
        workAnalysisService.recordActualCommute(request);
        return ResponseEntity.ok(ApiResponse.success(null, "通勤记录保存成功"));
    }

    /**
     * 获取通勤统计
     */
    @GetMapping("/{id}/commute/statistics")
    public ResponseEntity<ApiResponse<CommuteStatisticsResponse>> getCommuteStatistics(
            @PathVariable String id) {
        CommuteStatisticsResponse response = workAnalysisService.getCommuteStatistics(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

// 统一响应格式
@Data
class ApiResponse<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("操作成功");
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(500);
        response.setMessage(message);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}
