package com.example.drools.dto;

import lombok.extern.slf4j.Slf4j;
import org.kie.internal.command.CommandFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import org.kie.api.command.Command;
import java.util.List;

@Slf4j
public class DrlExecute {

    private static final DecimalFormat df = new DecimalFormat("######0.00");

    /**
     * 判断购物车中所有参加的活动商品
     *
     * @return 结果
     */
    public static RuleResult rulePromote(PromoteExecute promoteExecute, Double moneySum) {
        // 判断业务规则是否存在
        RuleResult ruleresult = new RuleResult();
        //统计所有参加活动商品的件数和金额
        ruleresult.setMoneySum(moneySum);//返回优惠前的价格
        log.info("优惠前的价格{}", moneySum);
        //统计完成后再将参数insert促销规则中
        List<Command> cmdCondition = new ArrayList<>();
        cmdCondition.add(CommandFactory.newInsert(ruleresult));

        promoteExecute.getWorkSession().execute(CommandFactory.newBatchExecution(cmdCondition));
        log.info("优惠后的价格{}", ruleresult.getFinallyMoney());
        return ruleresult;
    }
}
