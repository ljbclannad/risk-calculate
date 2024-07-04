package com.example.drools.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("t_index_risk_group")
@Data
public class IndexRiskGroup {

    @TableId
    private Integer id;

    private String groupName;

    private Integer risk;
}
