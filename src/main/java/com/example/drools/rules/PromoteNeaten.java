package com.example.drools.rules;

import com.example.drools.dto.PromoteExecute;
import org.springframework.stereotype.Service;

@Service
public class PromoteNeaten {

    public PromoteExecute editRule(String rule) throws RuntimeException {
        PromoteExecute promoteExecute = new PromoteExecute();
        promoteExecute.setWorkContent(rule);//促销业务规则
        //规则库 初始化
        promoteExecute.getWorkSession();
        return promoteExecute;
    }
}
