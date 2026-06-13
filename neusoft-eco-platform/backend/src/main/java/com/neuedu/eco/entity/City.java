package com.neuedu.eco.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("city")
public class City {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long provinceId;

    private String cityCode;

    private String cityName;
}
