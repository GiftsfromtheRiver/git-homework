package com.neuedu.eco.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("measure_data")
public class MeasureData {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long feedbackId;

    private Long gridMemberId;

    private Integer aqiValue;

    private BigDecimal pm25;
    private BigDecimal pm10;
    private BigDecimal so2;
    private BigDecimal no2;
    private BigDecimal co;
    private BigDecimal o3;

    private LocalDateTime measureTime;

    /** 1-一致，2-不一致 */
    private Integer confirmResult;
}
