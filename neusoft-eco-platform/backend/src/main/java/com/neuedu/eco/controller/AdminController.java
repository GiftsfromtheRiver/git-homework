package com.neuedu.eco.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neuedu.eco.dto.FeedbackAssignDTO;
import com.neuedu.eco.dto.FeedbackAssignInfoVO;
import com.neuedu.eco.dto.FeedbackVO;
import com.neuedu.eco.dto.Result;
import com.neuedu.eco.entity.Feedback;
import com.neuedu.eco.entity.GridMember;
import com.neuedu.eco.mapper.FeedbackMapper;
import com.neuedu.eco.mapper.GridMemberMapper;
import com.neuedu.eco.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final FeedbackService feedbackService;
    private final FeedbackMapper feedbackMapper;
    private final GridMemberMapper gridMemberMapper;

    /**
     * 查询反馈列表（支持状态筛选+分页）
     */
    @GetMapping("/feedback/list")
    public Result<IPage<FeedbackVO>> listFeedback(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        IPage<Feedback> page = feedbackService.lambdaQuery()
                .eq(status != null, Feedback::getStatus, status)
                .orderByDesc(Feedback::getFeedbackTime)
                .page(new Page<>(pageNum, pageSize));

        // 转换为VO
        IPage<FeedbackVO> voPage = page.convert(feedbackService::convertToVO);
        return Result.success("查询成功", voPage);
    }

    /**
     * 获取反馈详情
     */
    @GetMapping("/feedback/{id}")
    public Result<FeedbackVO> getFeedbackDetail(@PathVariable Long id) {
        FeedbackVO vo = feedbackService.getFeedbackVO(id);
        if (vo == null) {
            return Result.error("反馈记录不存在");
        }
        return Result.success(vo);
    }

    /**
     * 管理员指派反馈给网格员
     */
    @PutMapping("/feedback/assign")
    public Result<Map<String, Object>> assignFeedback(@Valid @RequestBody FeedbackAssignDTO dto) {
        try {
            feedbackService.assignFeedback(dto.getFeedbackId(), dto.getGridMemberId());
            Map<String, Object> data = new HashMap<>();
            data.put("feedbackId", dto.getFeedbackId());
            data.put("assignTime", LocalDateTime.now());
            return Result.success("指派成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取反馈指派信息（本市网格员列表+外省网格员列表）
     * 用于智能指派功能：本市优先、外省兜底
     */
    @GetMapping("/feedback/{id}/assign-info")
    public Result<FeedbackAssignInfoVO> getAssignInfo(@PathVariable Long id) {
        try {
            FeedbackAssignInfoVO info = feedbackService.getAssignInfo(id);
            return Result.success("查询成功", info);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 智能指派反馈给网格员（带城市权限校验）
     * 本市指派：校验网格员属于本市
     * 跨省指派：校验本市确无可用网格员
     */
    @PutMapping("/feedback/smart-assign")
    public Result<Map<String, Object>> smartAssignFeedback(@RequestBody Map<String, Object> params) {
        try {
            Long feedbackId = Long.valueOf(params.get("feedbackId").toString());
            Long gridMemberId = Long.valueOf(params.get("gridMemberId").toString());
            boolean isCrossProvince = Boolean.TRUE.equals(params.get("isCrossProvince"));

            feedbackService.smartAssign(feedbackId, gridMemberId, isCrossProvince);

            Map<String, Object> data = new HashMap<>();
            data.put("feedbackId", feedbackId);
            data.put("assignTime", LocalDateTime.now());
            return Result.success("指派成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取网格员列表（用于指派下拉选择）
     */
    @GetMapping("/grid-members")
    public Result<List<GridMember>> listGridMembers(
            @RequestParam(required = false) Long cityId) {
        List<GridMember> list = gridMemberMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GridMember>()
                        .eq(GridMember::getStatus, 1)
                        .eq(cityId != null, GridMember::getCityId, cityId)
        );
        return Result.success(list);
    }
}
