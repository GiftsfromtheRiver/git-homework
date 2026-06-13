package com.neuedu.eco.service.impl;

import com.neuedu.eco.dto.FeedbackSubmitDTO;
import com.neuedu.eco.entity.Feedback;
import com.neuedu.eco.entity.PublicSupervisor;
import com.neuedu.eco.mapper.FeedbackMapper;
import com.neuedu.eco.mapper.PublicSupervisorMapper;
import com.neuedu.eco.service.PublicFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PublicFeedbackServiceImpl implements PublicFeedbackService {

    private final FeedbackMapper feedbackMapper;
    private final PublicSupervisorMapper publicSupervisorMapper;

    @Override
    @Transactional
    public Map<String, Object> submitFeedback(FeedbackSubmitDTO dto) {
        // 1. 校验用户是否存在
        PublicSupervisor supervisor = publicSupervisorMapper.selectById(dto.getUserId());
        if (supervisor == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 构建反馈实体
        Feedback feedback = new Feedback();
        feedback.setPublicUserId(dto.getUserId());
        feedback.setGridAddress(dto.getGridAddress());
        feedback.setAqiValue(dto.getAqiValue());
        feedback.setPm25(dto.getPm25());
        feedback.setPm10(dto.getPm10());
        feedback.setSo2(dto.getSo2());
        feedback.setNo2(dto.getNo2());
        feedback.setCo(dto.getCo());
        feedback.setO3(dto.getO3());
        feedback.setFeedbackTime(LocalDateTime.now());
        feedback.setStatus(0); // 未指派

        // 3. 保存反馈
        feedbackMapper.insert(feedback);

        // 4. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("feedbackId", feedback.getId());
        result.put("status", "待指派");
        return result;
    }
}
