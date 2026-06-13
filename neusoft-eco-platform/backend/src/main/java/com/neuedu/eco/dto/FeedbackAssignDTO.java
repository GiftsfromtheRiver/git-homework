package com.neuedu.eco.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class FeedbackAssignDTO {

    @NotNull(message = "管理员ID不能为空")
    private Long adminId;

    @NotNull(message = "反馈ID不能为空")
    private Long feedbackId;

    @NotNull(message = "网格员ID不能为空")
    private Long gridMemberId;
}
