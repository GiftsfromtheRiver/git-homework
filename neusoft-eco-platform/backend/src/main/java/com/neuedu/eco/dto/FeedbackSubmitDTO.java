package com.neuedu.eco.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class FeedbackSubmitDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "网格地址不能为空")
    private String gridAddress;

    @NotNull(message = "AQI数值不能为空")
    private Integer aqiValue;

    private BigDecimal pm25;

    private BigDecimal pm10;

    private BigDecimal so2;

    private BigDecimal no2;

    private BigDecimal co;

    private BigDecimal o3;
}
