package com.neuedu.eco.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neuedu.eco.dto.MeasureConfirmDTO;
import com.neuedu.eco.dto.Result;
import com.neuedu.eco.entity.Feedback;
import com.neuedu.eco.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/grid")
@RequiredArgsConstructor
public class GridMemberController {

    private final FeedbackService feedbackService;

    /**
     * 接口1：网格员查询指派给自己的监督信息列表
     */
    @GetMapping("/tasks/list")
    public Result<IPage<Map<String, Object>>> listTasks(
            @RequestParam Long gridMemberId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            IPage<Map<String, Object>> page = feedbackService.listGridTasks(gridMemberId, pageNum, pageSize);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 接口2：网格员查询任务详情
     */
    @GetMapping("/tasks/{id}")
    public Result<Map<String, Object>> getTaskDetail(@PathVariable Long id) {
        try {
            Feedback feedback = feedbackService.getById(id);
            if (feedback == null) {
                return Result.error("任务不存在");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("feedbackId", feedback.getId());
            map.put("gridAddress", feedback.getGridAddress());
            map.put("aqiValue", feedback.getAqiValue());
            map.put("pm25", feedback.getPm25());
            map.put("pm10", feedback.getPm10());
            map.put("so2", feedback.getSo2());
            map.put("no2", feedback.getNo2());
            map.put("co", feedback.getCo());
            map.put("o3", feedback.getO3());
            map.put("feedbackTime", feedback.getFeedbackTime());
            map.put("status", feedback.getStatus());
            map.put("statusText", getStatusText(feedback.getStatus()));
            map.put("aqiLevel", getAqiLevelText(feedback.getAqiValue()));
            return Result.success("查询成功", map);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 接口3：网格员确认实测AQI数据
     */
    @PostMapping("/measure/confirm")
    public Result<Void> confirmMeasure(@Valid @RequestBody MeasureConfirmDTO dto) {
        try {
            feedbackService.confirmMeasure(dto);
            return Result.success("提交成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private String getAqiLevelText(Integer aqi) {
        if (aqi == null) return "未知";
        if (aqi <= 50) return "优";
        if (aqi <= 100) return "良";
        if (aqi <= 150) return "轻度污染";
        if (aqi <= 200) return "中度污染";
        if (aqi <= 300) return "重度污染";
        return "严重污染";
    }

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "未指派";
            case 1: return "待检测";
            case 2: return "已完成";
            default: return "未知";
        }
    }
}
