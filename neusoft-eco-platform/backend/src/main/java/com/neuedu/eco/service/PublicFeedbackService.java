package com.neuedu.eco.service;

import com.neuedu.eco.dto.FeedbackSubmitDTO;

import java.util.Map;

public interface PublicFeedbackService {

    Map<String, Object> submitFeedback(FeedbackSubmitDTO dto);
}
