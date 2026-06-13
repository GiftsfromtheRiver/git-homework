package com.neuedu.eco.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neuedu.eco.dto.FeedbackAssignInfoVO;
import com.neuedu.eco.dto.FeedbackVO;
import com.neuedu.eco.dto.MeasureConfirmDTO;
import com.neuedu.eco.entity.Feedback;

import java.util.Map;

public interface FeedbackService extends IService<Feedback> {

    IPage<Map<String, Object>> listGridTasks(Long gridMemberId, Integer pageNum, Integer pageSize);

    void confirmMeasure(MeasureConfirmDTO dto);

    void assignFeedback(Long feedbackId, Long gridMemberId);

    Map<String, Object> getAqiStatistics(String startDate, String endDate, Long provinceId, Long cityId, String dimension);

    /**
     * 获取反馈详情VO（包含关联信息）
     */
    FeedbackVO getFeedbackVO(Long id);

    /**
     * 将Feedback转换为FeedbackVO（填充关联信息）
     */
    FeedbackVO convertToVO(Feedback feedback);

    /**
     * 获取反馈指派信息（本市网格员、外省网格员列表）
     */
    FeedbackAssignInfoVO getAssignInfo(Long feedbackId);

    /**
     * 智能指派反馈给网格员（带城市权限校验）
     * @param feedbackId 反馈ID
     * @param gridMemberId 网格员ID
     * @param isCrossProvince 是否跨省指派
     */
    void smartAssign(Long feedbackId, Long gridMemberId, boolean isCrossProvince);

    /**
     * 按省份统计各类污染物超标次数
     */
    List<Map<String, Object>> getProvincePollutantOverCount();

    /**
     * 统计AQI等级分布
     */
    List<Map<String, Object>> getAqiLevelDistribution();

    /**
     * 统计月度AQI趋势
     */
    List<Map<String, Object>> getMonthlyAqiTrend();

    /**
     * 获取总体汇总数据
     */
    Map<String, Object> getOverallSummary();
}
