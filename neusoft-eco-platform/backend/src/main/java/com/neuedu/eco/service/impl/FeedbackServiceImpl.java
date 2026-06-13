package com.neuedu.eco.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.eco.dto.FeedbackAssignInfoVO;
import com.neuedu.eco.dto.FeedbackVO;
import com.neuedu.eco.dto.MeasureConfirmDTO;
import com.neuedu.eco.entity.City;
import com.neuedu.eco.entity.Feedback;
import com.neuedu.eco.entity.GridMember;
import com.neuedu.eco.entity.MeasureData;
import com.neuedu.eco.entity.Province;
import com.neuedu.eco.entity.PublicSupervisor;
import com.neuedu.eco.mapper.CityMapper;
import com.neuedu.eco.mapper.FeedbackMapper;
import com.neuedu.eco.mapper.GridMemberMapper;
import com.neuedu.eco.mapper.MeasureDataMapper;
import com.neuedu.eco.mapper.ProvinceMapper;
import com.neuedu.eco.mapper.PublicSupervisorMapper;
import com.neuedu.eco.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    private final FeedbackMapper feedbackMapper;
    private final MeasureDataMapper measureDataMapper;
    private final PublicSupervisorMapper publicSupervisorMapper;
    private final GridMemberMapper gridMemberMapper;
    private final CityMapper cityMapper;
    private final ProvinceMapper provinceMapper;

    @Override
    public IPage<Map<String, Object>> listGridTasks(Long gridMemberId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getAssignGridMemberId, gridMemberId)
               .orderByDesc(Feedback::getFeedbackTime);

        Page<Feedback> page = new Page<>(pageNum, pageSize);
        IPage<Feedback> feedbackPage = feedbackMapper.selectPage(page, wrapper);

        // 转换为Map列表，隐藏敏感字段
        IPage<Map<String, Object>> resultPage = feedbackPage.convert(f -> {
            Map<String, Object> map = new HashMap<>();
            map.put("feedbackId", f.getId());
            map.put("gridAddress", f.getGridAddress());
            map.put("aqiValue", f.getAqiValue());
            map.put("feedbackTime", f.getFeedbackTime());
            map.put("status", f.getStatus());
            return map;
        });

        return resultPage;
    }

    @Override
    @Transactional
    public void confirmMeasure(MeasureConfirmDTO dto) {
        // 1. 校验反馈是否存在
        Feedback feedback = feedbackMapper.selectById(dto.getFeedbackId());
        if (feedback == null) {
            throw new RuntimeException("反馈记录不存在");
        }

        // 2. 保存实测数据
        MeasureData measureData = new MeasureData();
        measureData.setFeedbackId(dto.getFeedbackId());
        measureData.setGridMemberId(dto.getGridMemberId());
        measureData.setAqiValue(dto.getAqiValue());
        measureData.setPm25(dto.getPm25());
        measureData.setPm10(dto.getPm10());
        measureData.setSo2(dto.getSo2());
        measureData.setNo2(dto.getNo2());
        measureData.setCo(dto.getCo());
        measureData.setO3(dto.getO3());
        measureData.setMeasureTime(LocalDateTime.now());
        measureData.setConfirmResult(dto.getConfirmResult());
        measureDataMapper.insert(measureData);

        // 3. 更新反馈状态为已确认
        feedback.setStatus(2);
        feedbackMapper.updateById(feedback);
    }

    @Override
    @Transactional
    public void assignFeedback(Long feedbackId, Long gridMemberId) {
        Feedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new RuntimeException("反馈记录不存在");
        }
        if (feedback.getStatus() != 0) {
            throw new RuntimeException("该反馈已被指派");
        }

        feedback.setAssignGridMemberId(gridMemberId);
        feedback.setStatus(1); // 已指派
        feedbackMapper.updateById(feedback);
    }

    @Override
    public Map<String, Object> getAqiStatistics(String startDate, String endDate, Long provinceId, Long cityId, String dimension) {
        // 1. 获取汇总数据
        Map<String, Object> summary = feedbackMapper.selectAqiSummary(startDate, endDate, provinceId, cityId);

        // 2. 获取趋势数据
        List<Map<String, Object>> trend = feedbackMapper.selectAqiTrend(startDate, endDate, provinceId, cityId, dimension);

        // 3. 组装结果
        Map<String, Object> result = new HashMap<>();
        if (summary != null) {
            result.put("avgAqi", summary.get("avgAqi"));
            result.put("maxAqi", summary.get("maxAqi"));
            result.put("minAqi", summary.get("minAqi"));
        }
        result.put("trend", trend);
        return result;
    }

    @Override
    public FeedbackVO getFeedbackVO(Long id) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            return null;
        }
        return convertToVO(feedback);
    }

    @Override
    public FeedbackVO convertToVO(Feedback feedback) {
        if (feedback == null) {
            return null;
        }
        FeedbackVO vo = new FeedbackVO();
        BeanUtils.copyProperties(feedback, vo);

        // 填充监督员信息
        if (feedback.getPublicUserId() != null) {
            PublicSupervisor supervisor = publicSupervisorMapper.selectById(feedback.getPublicUserId());
            if (supervisor != null) {
                vo.setSupervisorName(supervisor.getRealName());
                vo.setSupervisorPhone(supervisor.getPhone());
            }
        }

        // 填充城市名（从地址解析：取"市"之前的部分）
        String address = feedback.getGridAddress();
        if (address != null && address.contains("市")) {
            int idx = address.indexOf("市");
            vo.setCityName(address.substring(0, idx + 1));
        } else {
            vo.setCityName(address);
        }

        // 填充AQI等级
        vo.setAqiLevel(getAqiLevelText(feedback.getAqiValue()));

        // 填充状态文本
        vo.setStatusText(getStatusText(feedback.getStatus()));

        // 填充指派网格员姓名
        if (feedback.getAssignGridMemberId() != null) {
            GridMember gm = gridMemberMapper.selectById(feedback.getAssignGridMemberId());
            if (gm != null) {
                vo.setAssignGridMemberName(gm.getRealName());
            }
        }

        return vo;
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
            case 1: return "已指派";
            case 2: return "已确认";
            default: return "未知";
        }
    }

    @Override
    public FeedbackAssignInfoVO getAssignInfo(Long feedbackId) {
        // 1. 查询反馈
        Feedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new RuntimeException("反馈记录不存在");
        }

        // 2. 从地址解析城市ID（匹配city表）
        Long cityId = parseCityIdFromAddress(feedback.getGridAddress());
        Long provinceId = null;
        String cityName = "未知城市";

        if (cityId != null) {
            City city = cityMapper.selectById(cityId);
            if (city != null) {
                provinceId = city.getProvinceId();
                // 获取城市名称：直辖市显示省名，省辖地级市显示市名
                Province province = provinceMapper.selectById(provinceId);
                if (province != null) {
                    String provinceName = province.getProvinceName();
                    if (provinceName.endsWith("市")) {
                        // 直辖市，用省名作为城市名
                        cityName = provinceName;
                    } else {
                        // 省，用市名
                        cityName = city.getCityName();
                    }
                } else {
                    cityName = city.getCityName();
                }
            }
        }

        // 3. 查询本市网格员（同一省/直辖市下的所有网格员）
        List<GridMember> localMembers = new ArrayList<>();
        if (provinceId != null) {
            // 找出该省所有城市ID
            List<Long> provinceCityIds = cityMapper.selectList(
                    new LambdaQueryWrapper<City>()
                            .eq(City::getProvinceId, provinceId)
            ).stream().map(City::getId).toList();

            if (!provinceCityIds.isEmpty()) {
                localMembers = gridMemberMapper.selectList(
                        new LambdaQueryWrapper<GridMember>()
                                .eq(GridMember::getStatus, 1)
                                .in(GridMember::getCityId, provinceCityIds)
                );
            }
        }

        // 4. 查询外省网格员（不同省/直辖市的网格员）
        List<GridMember> otherProvinceMembers;
        if (provinceId != null) {
            List<Long> provinceCityIds = cityMapper.selectList(
                    new LambdaQueryWrapper<City>()
                            .eq(City::getProvinceId, provinceId)
            ).stream().map(City::getId).toList();

            otherProvinceMembers = gridMemberMapper.selectList(
                    new LambdaQueryWrapper<GridMember>()
                            .eq(GridMember::getStatus, 1)
                            .notIn(!provinceCityIds.isEmpty(), GridMember::getCityId, provinceCityIds)
            );
        } else {
            otherProvinceMembers = gridMemberMapper.selectList(
                    new LambdaQueryWrapper<GridMember>().eq(GridMember::getStatus, 1)
            );
        }

        // 5. 组装结果
        FeedbackAssignInfoVO vo = new FeedbackAssignInfoVO();
        vo.setFeedbackId(feedbackId);
        vo.setCityId(cityId);
        vo.setCityName(cityName);
        vo.setHasLocalMember(localMembers != null && !localMembers.isEmpty());
        vo.setLocalMembers(localMembers);
        vo.setOtherProvinceMembers(otherProvinceMembers);

        return vo;
    }

    @Override
    @Transactional
    public void smartAssign(Long feedbackId, Long gridMemberId, boolean isCrossProvince) {
        // 1. 校验反馈是否存在
        Feedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new RuntimeException("反馈记录不存在");
        }
        if (feedback.getStatus() != 0) {
            throw new RuntimeException("该反馈已被指派");
        }

        // 2. 校验网格员是否存在且启用
        GridMember member = gridMemberMapper.selectById(gridMemberId);
        if (member == null || member.getStatus() != 1) {
            throw new RuntimeException("网格员不存在或已禁用");
        }

        // 3. 解析反馈所在城市和省份
        Long feedbackCityId = parseCityIdFromAddress(feedback.getGridAddress());
        Long feedbackProvinceId = null;
        if (feedbackCityId != null) {
            City city = cityMapper.selectById(feedbackCityId);
            if (city != null) {
                feedbackProvinceId = city.getProvinceId();
            }
        }

        // 4. 城市权限校验
        if (!isCrossProvince) {
            // 本市指派：校验网格员是否属于本市（同一省/直辖市）
            if (feedbackProvinceId != null) {
                City memberCity = cityMapper.selectById(member.getCityId());
                if (memberCity == null || !feedbackProvinceId.equals(memberCity.getProvinceId())) {
                    throw new RuntimeException("该网格员不属于本市，无法进行本市指派");
                }
            }
        } else {
            // 跨省指派：校验本市确无可用网格员
            if (feedbackProvinceId != null) {
                // 找出该省所有城市ID
                List<Long> provinceCityIds = cityMapper.selectList(
                        new LambdaQueryWrapper<City>()
                                .eq(City::getProvinceId, feedbackProvinceId)
                ).stream().map(City::getId).toList();

                if (!provinceCityIds.isEmpty()) {
                    Long localCount = gridMemberMapper.selectCount(
                            new LambdaQueryWrapper<GridMember>()
                                    .eq(GridMember::getStatus, 1)
                                    .in(GridMember::getCityId, provinceCityIds)
                    );
                    if (localCount != null && localCount > 0) {
                        throw new RuntimeException("本市存在可用网格员，不得进行跨省指派");
                    }
                }
            }
        }

        // 5. 执行指派
        feedback.setAssignGridMemberId(gridMemberId);
        feedback.setStatus(1); // 已指派
        feedbackMapper.updateById(feedback);
    }

    /**
     * 从地址中解析城市ID
     * 遍历所有城市，取地址中包含的最长匹配城市名
     */
    private Long parseCityIdFromAddress(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }

        List<City> allCities = cityMapper.selectList(null);
        City matchedCity = null;
        int maxMatchLength = 0;

        for (City city : allCities) {
            String cityName = city.getCityName();
            if (address.contains(cityName) && cityName.length() > maxMatchLength) {
                matchedCity = city;
                maxMatchLength = cityName.length();
            }
        }

        return matchedCity != null ? matchedCity.getId() : null;
    }

    /**
     * 根据城市ID获取省份ID
     */
    private Long getProvinceIdByCityId(Long cityId) {
        if (cityId == null) return null;
        City city = cityMapper.selectById(cityId);
        return city != null ? city.getProvinceId() : null;
    }

    @Override
    public List<Map<String, Object>> getProvincePollutantOverCount() {
        return feedbackMapper.selectProvincePollutantOverCount();
    }

    @Override
    public List<Map<String, Object>> getAqiLevelDistribution() {
        return feedbackMapper.selectAqiLevelDistribution();
    }

    @Override
    public List<Map<String, Object>> getMonthlyAqiTrend() {
        return feedbackMapper.selectMonthlyAqiTrend();
    }

    @Override
    public Map<String, Object> getOverallSummary() {
        // 直接调用现有的汇总方法，不传时间和区域限制
        return feedbackMapper.selectAqiSummary(null, null, null, null);
    }
}
