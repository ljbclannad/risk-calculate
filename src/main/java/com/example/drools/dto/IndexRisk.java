package com.example.drools.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("t_index_risk_dic")
@Data
public class IndexRisk {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String chineseName;

    private String englishName;

    private String formula;

    private double defaultValue;

    private Integer risk;

    private Integer groupId;
}
