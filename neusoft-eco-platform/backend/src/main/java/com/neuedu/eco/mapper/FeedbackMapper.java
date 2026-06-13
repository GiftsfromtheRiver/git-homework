package com.neuedu.eco.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neuedu.eco.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {

    List<Map<String, Object>> selectAqiTrend(@Param("startDate") String startDate,
                                              @Param("endDate") String endDate,
                                              @Param("provinceId") Long provinceId,
                                              @Param("cityId") Long cityId,
                                              @Param("dimension") String dimension);

    Map<String, Object> selectAqiSummary(@Param("startDate") String startDate,
                                          @Param("endDate") String endDate,
                                          @Param("provinceId") Long provinceId,
                                          @Param("cityId") Long cityId);

    /**
     * 按省份统计各类污染物超标次数
     * 超标阈值：PM2.5 > 75, SO2 > 150, CO > 4
     */
    List<Map<String, Object>> selectProvincePollutantOverCount();

    /**
     * 统计AQI等级分布
     */
    List<Map<String, Object>> selectAqiLevelDistribution();

    /**
     * 统计月度AQI趋势
     */
    List<Map<String, Object>> selectMonthlyAqiTrend();
}
