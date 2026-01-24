package com.example.springboot002.demos.web.DTO.Request.WorkAnalysisRequest;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class WorkAnalysisRequest {

    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @DecimalMin(value = "0.0", inclusive = false, message = "月薪必须大于0")
    private BigDecimal monthlySalary;

    @DecimalMin(value = "0.0", inclusive = false, message = "年薪必须大于0")
    private BigDecimal annualSalary;

    @Min(value = 1, message = "工作日天数至少为1天")
    @Max(value = 31, message = "工作日天数不能超过31天")
    private Integer workDaysPerMonth = 22;

    @NotBlank(message = "家庭地址不能为空")
    private String homeAddress;

    @NotBlank(message = "公司地址不能为空")
    private String companyAddress;

    private Double homeLongitude;
    private Double homeLatitude;
    private Double companyLongitude;
    private Double companyLatitude;

    @NotNull(message = "通勤方式不能为空")
    private String commuteMode; // DRIVING, TRANSIT, WALKING, CYCLING, MIXED
}

