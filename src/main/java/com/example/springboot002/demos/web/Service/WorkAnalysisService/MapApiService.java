package com.example.springboot002.demos.web.Service.WorkAnalysisService;

import com.example.springboot002.demos.web.DTO.Response.WorkAnalysisResponse.CommuteCalculationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalTime;

@Slf4j
@Service
public class MapApiService {

    @Value("${map.api.key}")
    private String apiKey;

    @Value("${map.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MapApiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 计算通勤距离和时长
     */
    public CommuteCalculationResponse calculateCommute(String origin, String destination,
                                                       String mode, boolean isPeakHour) {
        try {
            String url = buildApiUrl(origin, destination, mode);
            log.info("调用地图API: {}", url);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            CommuteCalculationResponse result = parseApiResponse(root, mode);

            // 高峰期时间修正
            if (isPeakHour) {
                result.setCorrectedDuration(applyPeakCorrection(result.getDurationMinutes()));
            } else {
                result.setCorrectedDuration(result.getDurationMinutes());
            }

            return result;

        } catch (Exception e) {
            log.error("地图API调用失败", e);
            throw new RuntimeException("无法计算通勤时间,请检查地址信息", e);
        }
    }

    /**
     * 地理编码 - 地址转坐标
     */
    public double[] geocode(String address) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/geocode/geo")
                    .queryParam("key", apiKey)
                    .queryParam("address", address)
                    .build()
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if ("1".equals(root.path("status").asText())) {
                String location = root.path("geocodes").get(0).path("location").asText();
                String[] coords = location.split(",");
                return new double[]{Double.parseDouble(coords[0]), Double.parseDouble(coords[1])};
            }

            throw new RuntimeException("地址解析失败");

        } catch (Exception e) {
            log.error("地理编码失败: {}", address, e);
            throw new RuntimeException("地址解析失败", e);
        }
    }

    /**
     * 构建API URL
     */
    private String buildApiUrl(String origin, String destination, String mode) {
        String apiMode = convertToApiMode(mode);
        String endpoint = "TRANSIT".equals(apiMode) ? "/direction/transit/integrated" : "/direction/driving";

        return UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint)
                .queryParam("key", apiKey)
                .queryParam("origin", origin)
                .queryParam("destination", destination)
                .queryParam("extensions", "base")
                .build()
                .toUriString();
    }

    /**
     * 解析API响应
     */
    private CommuteCalculationResponse parseApiResponse(JsonNode root, String mode) {
        CommuteCalculationResponse response = new CommuteCalculationResponse();

        if (!"1".equals(root.path("status").asText())) {
            throw new RuntimeException("API返回错误: " + root.path("info").asText());
        }

        JsonNode route;
        if ("TRANSIT".equals(convertToApiMode(mode))) {
            route = root.path("route").path("transits").get(0);
        } else {
            route = root.path("route").path("paths").get(0);
        }

        int distance = route.path("distance").asInt();
        int duration = route.path("duration").asInt() / 60; // 秒转分钟

        response.setDistanceMeters(distance);
        response.setDistanceText(formatDistance(distance));
        response.setDurationMinutes(duration);
        response.setDurationText(duration + "分钟");

        return response;
    }

    /**
     * 高峰期时间修正
     */
    private Integer applyPeakCorrection(Integer baseDuration) {
        BigDecimal correction = getPeakCorrectionFactor();
        return BigDecimal.valueOf(baseDuration)
                .multiply(correction)
                .intValue();
    }

    /**
     * 获取高峰期修正系数
     */
    private BigDecimal getPeakCorrectionFactor() {
        LocalTime now = LocalTime.now();

        // 早高峰 7:00-9:30
        if (now.isAfter(LocalTime.of(7, 0)) && now.isBefore(LocalTime.of(9, 30))) {
            return new BigDecimal("1.3");
        }
        // 晚高峰 17:00-19:30
        if (now.isAfter(LocalTime.of(17, 0)) && now.isBefore(LocalTime.of(19, 30))) {
            return new BigDecimal("1.25");
        }

        return BigDecimal.ONE;
    }

    /**
     * 转换通勤模式到API参数
     */
    private String convertToApiMode(String mode) {
        if (mode == null) {
            return "DRIVING";
        }

        switch (mode.toUpperCase()) {
            case "DRIVING":
                return "DRIVING";
            case "TRANSIT":
                return "TRANSIT";
            case "WALKING":
                return "WALKING";
            case "CYCLING":
                return "BICYCLING";
            default:
                return "DRIVING";
        }
    }

    /**
     * 格式化距离
     */
    private String formatDistance(int meters) {
        if (meters < 1000) {
            return meters + "米";
        }
        return String.format("%.1f公里", meters / 1000.0);
    }
}