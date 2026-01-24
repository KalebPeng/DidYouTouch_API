package com.example.springboot002.demos.web.Service.WorkAnalysisService;

import com.example.springboot002.demos.web.DTO.Request.WorkAnalysisRequest.ActualCommuteRequest;
import com.example.springboot002.demos.web.DTO.Request.WorkAnalysisRequest.CommuteCalculationRequest;
import com.example.springboot002.demos.web.DTO.Request.WorkAnalysisRequest.WorkAnalysisRequest;
import com.example.springboot002.demos.web.DTO.Response.WorkAnalysisResponse.CommuteCalculationResponse;
import com.example.springboot002.demos.web.DTO.Response.WorkAnalysisResponse.CommuteStatisticsResponse;
import com.example.springboot002.demos.web.DTO.Response.WorkAnalysisResponse.WorkAnalysisResponse;
import com.example.springboot002.demos.web.Entity.WorkAnalysisEntity.CommuteMode;
import com.example.springboot002.demos.web.Entity.WorkAnalysisEntity.CommuteRecord;
import com.example.springboot002.demos.web.Entity.WorkAnalysisEntity.WorkAnalysis;
import com.example.springboot002.demos.web.Repository.WorkAnalysisRespository.CommuteRecordRepository;
import com.example.springboot002.demos.web.Repository.WorkAnalysisRespository.WorkAnalysisRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkAnalysisService {

    private final WorkAnalysisRepository workAnalysisRepository;
    private final CommuteRecordRepository commuteRecordRepository;
    private final MapApiService mapApiService;

    /**
     * 创建工作分析记录
     */
    @Transactional
    public WorkAnalysisResponse createWorkAnalysis(WorkAnalysisRequest request) {
        log.info("创建工作分析记录: userId={}", request.getUserId());

        WorkAnalysis analysis = new WorkAnalysis();
        analysis.setUserId(request.getUserId());
        analysis.setMonthlySalary(request.getMonthlySalary());
        analysis.setAnnualSalary(request.getAnnualSalary());
        analysis.setWorkDaysPerMonth(request.getWorkDaysPerMonth());
        analysis.setHomeAddress(request.getHomeAddress());
        analysis.setCompanyAddress(request.getCompanyAddress());
        analysis.setCommuteMode(parseCommuteMode(request.getCommuteMode()));

        // 地址转坐标
        if (request.getHomeLongitude() == null || request.getHomeLatitude() == null) {
            double[] homeCoords = mapApiService.geocode(request.getHomeAddress());
            analysis.setHomeLongitude(homeCoords[0]);
            analysis.setHomeLatitude(homeCoords[1]);
        } else {
            analysis.setHomeLongitude(request.getHomeLongitude());
            analysis.setHomeLatitude(request.getHomeLatitude());
        }

        if (request.getCompanyLongitude() == null || request.getCompanyLatitude() == null) {
            double[] companyCoords = mapApiService.geocode(request.getCompanyAddress());
            analysis.setCompanyLongitude(companyCoords[0]);
            analysis.setCompanyLatitude(companyCoords[1]);
        } else {
            analysis.setCompanyLongitude(request.getCompanyLongitude());
            analysis.setCompanyLatitude(request.getCompanyLatitude());
        }

        WorkAnalysis saved = workAnalysisRepository.save(analysis);

        // 计算初始通勤信息
        calculateAndSaveCommuteInfo(saved);

        return convertToResponse(saved);
    }
    /**
     * 获取工作分析记录
     */
    public WorkAnalysisResponse getWorkAnalysis(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "ID cannot be null or empty"
            );
        }

        WorkAnalysis analysis = workAnalysisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Work analysis not found with id: " + id
                ));

        return convertToResponse(analysis);
    }

    /**
     * 根据用户ID获取工作分析记录
     */
    public WorkAnalysisResponse getWorkAnalysisByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "ID cannot be null or empty"
            );
        }

        WorkAnalysis analysis = workAnalysisRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Work analysis not found with id: " + userId
                ));

        return convertToResponse(analysis);
    }

    /**
     * 更新工作分析记录
     */
    @Transactional
    public WorkAnalysisResponse updateWorkAnalysis(String id, WorkAnalysisRequest request) {
        WorkAnalysis analysis = workAnalysisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("工作分析记录不存在"));

        if (request.getMonthlySalary() != null) {
            analysis.setMonthlySalary(request.getMonthlySalary());
        }
        if (request.getAnnualSalary() != null) {
            analysis.setAnnualSalary(request.getAnnualSalary());
        }
        if (request.getWorkDaysPerMonth() != null) {
            analysis.setWorkDaysPerMonth(request.getWorkDaysPerMonth());
        }

        // 如果地址变更,重新计算坐标和通勤信息
        boolean addressChanged = false;
        if (request.getHomeAddress() != null && !request.getHomeAddress().equals(analysis.getHomeAddress())) {
            analysis.setHomeAddress(request.getHomeAddress());
            double[] coords = mapApiService.geocode(request.getHomeAddress());
            analysis.setHomeLongitude(coords[0]);
            analysis.setHomeLatitude(coords[1]);
            addressChanged = true;
        }

        if (request.getCompanyAddress() != null && !request.getCompanyAddress().equals(analysis.getCompanyAddress())) {
            analysis.setCompanyAddress(request.getCompanyAddress());
            double[] coords = mapApiService.geocode(request.getCompanyAddress());
            analysis.setCompanyLongitude(coords[0]);
            analysis.setCompanyLatitude(coords[1]);
            addressChanged = true;
        }

        if (request.getCommuteMode() != null) {
            analysis.setCommuteMode(parseCommuteMode(request.getCommuteMode()));
            addressChanged = true;
        }

        WorkAnalysis updated = workAnalysisRepository.save(analysis);

        if (addressChanged) {
            calculateAndSaveCommuteInfo(updated);
        }

        return convertToResponse(updated);
    }

    /**
     * 计算通勤信息
     */
    public CommuteCalculationResponse calculateCommuteTime(CommuteCalculationRequest request) {
        WorkAnalysis analysis = workAnalysisRepository.findById(request.getWorkAnalysisId())
                .orElseThrow(() -> new RuntimeException("工作分析记录不存在"));

        String origin = request.getOrigin() != null ? request.getOrigin()
                : analysis.getHomeLongitude() + "," + analysis.getHomeLatitude();
        String destination = request.getDestination() != null ? request.getDestination()
                : analysis.getCompanyLongitude() + "," + analysis.getCompanyLatitude();
        String mode = request.getMode() != null ? request.getMode()
                : analysis.getCommuteMode().name();
        boolean isPeakHour = request.getIsPeakHour() != null ? request.getIsPeakHour()
                : isCurrentlyPeakHour();

        return mapApiService.calculateCommute(origin, destination, mode, isPeakHour);
    }

    /**
     * 记录实际通勤时间
     */
    @Transactional
    public void recordActualCommute(ActualCommuteRequest request) {
        CommuteRecord record = new CommuteRecord();
        record.setWorkAnalysisId(request.getWorkAnalysisId());
        record.setCommuteType(request.getCommuteType());
        record.setActualDurationMinutes(request.getActualDurationMinutes());
        record.setCommuteModes(request.getCommuteModes());
        record.setIsPeakHour(isCurrentlyPeakHour());
        record.setRecordDate(LocalDateTime.now());

        commuteRecordRepository.save(record);
        log.info("记录实际通勤时间: {}分钟, 类型: {}",
                request.getActualDurationMinutes(), request.getCommuteType());
    }

    /**
     * 获取通勤统计信息
     */
    public CommuteStatisticsResponse getCommuteStatistics(String workAnalysisId) {
        Double avgMorning = commuteRecordRepository.getAverageDurationByType(workAnalysisId, "morning");
        Double avgEvening = commuteRecordRepository.getAverageDurationByType(workAnalysisId, "evening");

        List<CommuteRecord> recentRecords = commuteRecordRepository
                .findByWorkAnalysisIdOrderByRecordDateDesc(workAnalysisId);

        CommuteStatisticsResponse response = new CommuteStatisticsResponse();
        response.setAverageMorningDuration(avgMorning != null ? avgMorning.intValue() : null);
        response.setAverageEveningDuration(avgEvening != null ? avgEvening.intValue() : null);
        response.setTotalRecords(recentRecords.size());

        return response;
    }

    // ========== 私有辅助方法 ==========

    private void calculateAndSaveCommuteInfo(WorkAnalysis analysis) {
        String origin = analysis.getHomeLongitude() + "," + analysis.getHomeLatitude();
        String destination = analysis.getCompanyLongitude() + "," + analysis.getCompanyLatitude();

        // 计算早高峰通勤
        CommuteCalculationResponse morning = mapApiService.calculateCommute(
                origin, destination, analysis.getCommuteMode().name(), true);

        CommuteRecord morningRecord = new CommuteRecord();
        morningRecord.setWorkAnalysisId(analysis.getId());
        morningRecord.setCommuteType("morning");
        morningRecord.setDistanceMeters(morning.getDistanceMeters());
        morningRecord.setApiDurationMinutes(morning.getDurationMinutes());
        morningRecord.setDurationMinutes(morning.getCorrectedDuration());
        morningRecord.setIsPeakHour(true);
        morningRecord.setPeakCorrectionFactor(new BigDecimal("1.3"));
        commuteRecordRepository.save(morningRecord);

        // 计算晚高峰通勤
        CommuteCalculationResponse evening = mapApiService.calculateCommute(
                destination, origin, analysis.getCommuteMode().name(), true);

        CommuteRecord eveningRecord = new CommuteRecord();
        eveningRecord.setWorkAnalysisId(analysis.getId());
        eveningRecord.setCommuteType("evening");
        eveningRecord.setDistanceMeters(evening.getDistanceMeters());
        eveningRecord.setApiDurationMinutes(evening.getDurationMinutes());
        eveningRecord.setDurationMinutes(evening.getCorrectedDuration());
        eveningRecord.setIsPeakHour(true);
        eveningRecord.setPeakCorrectionFactor(new BigDecimal("1.25"));
        commuteRecordRepository.save(eveningRecord);
    }

    private boolean isCurrentlyPeakHour() {
        LocalTime now = LocalTime.now();
        return (now.isAfter(LocalTime.of(7, 0)) && now.isBefore(LocalTime.of(9, 30))) ||
                (now.isAfter(LocalTime.of(17, 0)) && now.isBefore(LocalTime.of(19, 30)));
    }

    private CommuteMode parseCommuteMode(String mode) {
        return CommuteMode.valueOf(mode.toUpperCase());
    }

    private WorkAnalysisResponse convertToResponse(WorkAnalysis analysis) {
        WorkAnalysisResponse response = new WorkAnalysisResponse();
        response.setId(analysis.getId());
        response.setUserId(analysis.getUserId());
        response.setMonthlySalary(analysis.getMonthlySalary());
        response.setAnnualSalary(analysis.getAnnualSalary());
        response.setWorkDaysPerMonth(analysis.getWorkDaysPerMonth());
        response.setHomeAddress(analysis.getHomeAddress());
        response.setCompanyAddress(analysis.getCompanyAddress());
        response.setCommuteMode(analysis.getCommuteMode().name());
        return response;
    }
}