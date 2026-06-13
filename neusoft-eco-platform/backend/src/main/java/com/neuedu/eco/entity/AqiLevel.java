package com.neuedu.eco.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("aqi_level")
public class AqiLevel {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer aqiMin;

    private Integer aqiMax;

    private String level;

    private String description;

    private String color;
}
