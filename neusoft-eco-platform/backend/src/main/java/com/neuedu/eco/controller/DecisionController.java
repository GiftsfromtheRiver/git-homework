package com.neuedu.eco.controller;

import com.neuedu.eco.dto.AqiStatisticsDTO;
import com.neuedu.eco.dto.Result;
import com.neuedu.eco.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/decision/statistics")
@RequiredArgsConstructor
public class DecisionController {

    private final FeedbackService feedbackService;

    /**
     * 接口5：决策者获取AQI统计数据（用于可视化大屏）
     */
    @GetMapping("/aqi")
    public Result<Map<String, Object>> getAqiStatistics(AqiStatisticsDTO dto) {
        try {
            Map<String, Object> data = feedbackService.getAqiStatistics(
                    dto.getStartDate(), dto.getEndDate(),
                    dto.getProvinceId(), dto.getCityId(),
                    dto.getDimension()
            );
            return Result.success("查询成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
