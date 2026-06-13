package com.neuedu.eco.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 反馈详情VO（包含关联信息）
 */
@Data
public class FeedbackVO {

    private Long id;

    private Long publicUserId;

    /** 监督员姓名 */
    private String supervisorName;

    /** 监督员电话 */
    private String supervisorPhone;

    private String gridAddress;

    /** 城市名称（从地址解析） */
    private String cityName;

    private Integer aqiValue;

    /** AQI等级描述 */
    private String aqiLevel;

    private BigDecimal pm25;

    private BigDecimal pm10;

    private BigDecimal so2;

    private BigDecimal no2;

    private BigDecimal co;

    private BigDecimal o3;

    private LocalDateTime feedbackTime;

    /** 0-未指派，1-已指派，2-已确认 */
    private Integer status;

    /** 状态描述 */
    private String statusText;

    private Long assignGridMemberId;

    /** 指派网格员姓名 */
    private String assignGridMemberName;
}
