package com.neuedu.eco.dto;

import com.neuedu.eco.entity.GridMember;
import lombok.Data;

import java.util.List;

/**
 * 反馈指派信息VO
 * 包含本市网格员、外省网格员列表及状态
 */
@Data
public class FeedbackAssignInfoVO {

    /** 反馈ID */
    private Long feedbackId;

    /** 反馈所在城市名称 */
    private String cityName;

    /** 反馈所在城市ID */
    private Long cityId;

    /** 是否有本市可用网格员 */
    private Boolean hasLocalMember;

    /** 本市网格员列表 */
    private List<GridMember> localMembers;

    /** 外省网格员列表 */
    private List<GridMember> otherProvinceMembers;
}
