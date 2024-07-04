package com.example.drools.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("t_index_risk_formula")
@Data
public class IndexRiskFormula {

    @TableId
    private Integer id;

    private String formula;

    private String needResult;

    private String indexDicName;
}
