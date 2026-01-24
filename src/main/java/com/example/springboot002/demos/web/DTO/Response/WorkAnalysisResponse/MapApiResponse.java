package com.example.springboot002.demos.web.DTO.Response.WorkAnalysisResponse;

import lombok.Data;

@Data
public class MapApiResponse {
    private String status;
    private String info;
    private Object data; // 原始API返回数据
}
