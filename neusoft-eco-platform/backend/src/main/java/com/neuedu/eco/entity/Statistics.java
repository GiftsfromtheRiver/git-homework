package com.neuedu.eco.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("statistics")
public class Statistics {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String gridAddress;

    private Integer aqiValue;

    private BigDecimal pm25;

    private LocalDateTime measureTime;

    private Long gridMemberId;
}
