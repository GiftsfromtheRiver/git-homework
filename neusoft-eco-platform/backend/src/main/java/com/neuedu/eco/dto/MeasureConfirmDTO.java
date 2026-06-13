package com.neuedu.eco.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class MeasureConfirmDTO {

    @NotNull(message = "反馈ID不能为空")
    private Long feedbackId;

    @NotNull(message = "网格员ID不能为空")
    private Long gridMemberId;

    @NotNull(message = "实测AQI值不能为空")
    private Integer aqiValue;

    private BigDecimal pm25;
    private BigDecimal pm10;
    private BigDecimal so2;
    private BigDecimal no2;
    private BigDecimal co;
    private BigDecimal o3;

    @NotNull(message = "确认结果不能为空")
    private Integer confirmResult;
}
