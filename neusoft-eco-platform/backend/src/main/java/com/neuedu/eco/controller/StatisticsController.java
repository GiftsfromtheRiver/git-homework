package com.neuedu.eco.controller;

import com.neuedu.eco.dto.Result;
import com.neuedu.eco.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final FeedbackService feedbackService;

    /**
     * 大屏总览接口
     * 整合省份污染物统计、AQI分布、月度趋势等全量大屏数据
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData() {
        try {
            Map<String, Object> data = new HashMap<>();

            // 1. 各省份污染物超标统计
            List<Map<String, Object>> provinceStats = feedbackService.getProvincePollutantOverCount();
            data.put("provincePollutantStats", provinceStats);

            // 2. AQI等级分布
            List<Map<String, Object>> aqiDistribution = feedbackService.getAqiLevelDistribution();
            data.put("aqiDistribution", aqiDistribution);

            // 3. 月度AQI趋势
            List<Map<String, Object>> monthlyTrend = feedbackService.getMonthlyAqiTrend();
            data.put("monthlyTrend", monthlyTrend);

            // 4. 汇总数据
            Map<String, Object> summary = feedbackService.getOverallSummary();
            data.put("summary", summary);

            return Result.success("查询成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
