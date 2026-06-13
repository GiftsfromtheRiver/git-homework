package com.neuedu.eco.controller;

import com.neuedu.eco.dto.FeedbackSubmitDTO;
import com.neuedu.eco.dto.Result;
import com.neuedu.eco.service.PublicFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/public/feedback")
@RequiredArgsConstructor
public class PublicFeedbackController {

    private final PublicFeedbackService publicFeedbackService;

    /**
     * 接口1：公众监督员提交AQI反馈
     */
    @PostMapping("/submit")
    public Result<Map<String, Object>> submitFeedback(@Valid @RequestBody FeedbackSubmitDTO dto) {
        try {
            Map<String, Object> result = publicFeedbackService.submitFeedback(dto);
            return Result.success("反馈提交成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
