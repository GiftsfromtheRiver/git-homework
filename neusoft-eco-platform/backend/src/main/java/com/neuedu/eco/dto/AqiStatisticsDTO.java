package com.neuedu.eco.dto;

import lombok.Data;

@Data
public class AqiStatisticsDTO {

    private String startDate;

    private String endDate;

    private Long provinceId;

    private Long cityId;

    private String dimension = "day";
}
