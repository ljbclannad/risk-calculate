package com.example.drools.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.drools.config.DefaultIndexRisk;
import com.example.drools.dto.IndexRisk;
import com.example.drools.dto.IndexRiskFormula;
import com.example.drools.dto.IndexRiskGroup;
import com.example.drools.dto.TestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class RiskService {

    private static final String GREATER_EQUAL = ">=";
    private static final String LESS_EQUAL = "<=";
    private static final String GREATER = ">";
    private static final String LESS = "<";

    @Autowired
    DefaultIndexRisk defaultIndexRisk;

    public void refresh() {
        defaultIndexRisk.init();
    }

    public Integer calculate(JSONObject data) throws ScriptException {
        //获取所有配置项
        List<IndexRisk> indexRiskList = defaultIndexRisk.indexRiskList;
        List<IndexRiskGroup> indexRiskGroupList = defaultIndexRisk.indexRiskGroupList;
        List<IndexRiskFormula> indexRiskFormulaList = defaultIndexRisk.indexRiskFormulaList;


        //获取入参，参数格式为{
        //    "data": {
        //        "全血血红蛋白": "123",
        //        "白细胞计数": "7"
        //    }
        //}
        List<TestResult> testResultList = new ArrayList<>();
        JSONObject result = data.getJSONObject("data");
        result.forEach((key, value) -> {
            TestResult testResult = new TestResult();
            testResult.setName(key);
            testResult.setValue(parseDoubleOrDefault(value));
            testResultList.add(testResult);
        });

        //默认为低风险
        AtomicInteger risk = new AtomicInteger();

        //进行配置项和参数的比较
        //1. 比较单个指标值的风险状态
        indexRiskList.forEach(indexRisk -> testResultList.stream()
                .filter(testResult -> testResult.getName().equals(indexRisk.getChineseName()))
                .filter(testResult -> (GREATER_EQUAL.equals(indexRisk.getFormula()) && testResult.getValue() >= indexRisk.getDefaultValue()) ||
                        (LESS_EQUAL.equals(indexRisk.getFormula()) && testResult.getValue() <= indexRisk.getDefaultValue()) ||
                        (GREATER.equals(indexRisk.getFormula()) && testResult.getValue() > indexRisk.getDefaultValue()) ||
                        (LESS.equals(indexRisk.getFormula()) && testResult.getValue() < indexRisk.getDefaultValue())
                )
                .forEach(testResult -> risk.updateAndGet(currentRisk -> Math.max(currentRisk, indexRisk.getRisk()))));

        //2. 比较分组的风险状态（多条件同时满足）
        indexRiskGroupList.forEach(indexRiskGroup -> {
            long matchCount = indexRiskList.stream()
                    .filter(indexRisk -> indexRiskGroup.getId().equals(indexRisk.getGroupId()))
                    .filter(indexRisk -> testResultList.stream()
                            .anyMatch(testResult -> testResult.getName().equals(indexRisk.getChineseName())
                                    &&
                                    (GREATER_EQUAL.equals(indexRisk.getFormula()) && testResult.getValue() >= indexRisk.getDefaultValue() ||
                                            LESS_EQUAL.equals(indexRisk.getFormula()) && testResult.getValue() <= indexRisk.getDefaultValue() ||
                                            GREATER.equals(indexRisk.getFormula()) && testResult.getValue() > indexRisk.getDefaultValue() ||
                                            LESS.equals(indexRisk.getFormula()) && testResult.getValue() < indexRisk.getDefaultValue())
                            ))
                    .count();
            if (matchCount == indexRiskList.stream().filter(indexRisk -> indexRiskGroup.getId().equals(indexRisk.getGroupId())).count()) {
                risk.updateAndGet(currentRisk -> Math.max(currentRisk, indexRiskGroup.getRisk()));
            }
        });


        //3. 进行公式化的计算
        indexRiskFormulaList.forEach(indexRiskFormula -> {
            List<String> needResultList = Arrays.asList(indexRiskFormula.getNeedResult().split(","));
            if (needResultList.stream().allMatch(neededName -> testResultList.stream().anyMatch(testResult -> testResult.getName().equals(neededName)))) {
                AtomicReference<String> formula = new AtomicReference<>(indexRiskFormula.getFormula());

                //替换公式内容
                for (String neededName : needResultList) {
                    testResultList.stream()
                            .filter(testResult -> testResult.getName().equals(neededName))
                            .findFirst()
                            .ifPresent(testResult -> formula.set(formula.get().replace(neededName, String.valueOf(testResult.getValue()))));
                }
                String finalFormula = formula.get();
                ScriptEngineManager manager = new ScriptEngineManager();
                ScriptEngine engine = manager.getEngineByName("JavaScript");
                try {
                    //计算结果
                    Object formulaResult = engine.eval(finalFormula);
                    String indexDicName = indexRiskFormula.getIndexDicName();


                    //需要比较的风险配置
                    List<IndexRisk> formulaRiskList = indexRiskList.stream()
                            .filter(indexRisk -> indexRisk.getChineseName().equals(indexDicName)).collect(Collectors.toList());

                    //判断该值需要单独计算还是按区间计算
                    if (formulaRiskList.stream().allMatch(formulaRisk -> formulaRisk.getGroupId() == null)) {
                        //单独计算指标
                        formulaRiskList.stream()
                                .filter(formulaRisk -> (GREATER_EQUAL.equals(formulaRisk.getFormula()) && parseDoubleOrDefault(formulaResult) >= formulaRisk.getDefaultValue()) ||
                                        (LESS_EQUAL.equals(formulaRisk.getFormula()) && parseDoubleOrDefault(formulaResult) <= formulaRisk.getDefaultValue()) ||
                                        (GREATER.equals(formulaRisk.getFormula()) && parseDoubleOrDefault(formulaResult) > formulaRisk.getDefaultValue()) ||
                                        (LESS.equals(formulaRisk.getFormula()) && parseDoubleOrDefault(formulaResult) < formulaRisk.getDefaultValue())
                                )
                                .forEach(formulaRisk -> risk.updateAndGet(currentRisk -> Math.max(currentRisk, formulaRisk.getRisk())));
                    } else if (formulaRiskList.stream().allMatch(formulaRisk -> formulaRisk.getGroupId() != null)) {
                        //按区间计算 可能配置了多个区间，每个区间单独计算
                        Map<Integer, List<IndexRisk>> groupedFormulaRiskList = formulaRiskList.stream()
                                .collect(Collectors.groupingBy(IndexRisk::getGroupId));
                        groupedFormulaRiskList.forEach((groupId, groupFormulaRiskList) -> {
                            IndexRiskGroup formulaRiskGroup = indexRiskGroupList.stream()
                                    .filter(indexRiskGroup -> indexRiskGroup.getId().equals(groupFormulaRiskList.get(0).getGroupId()))
                                    .findFirst().orElse(null);

                            //比较满足个数
                            long matchCount = groupFormulaRiskList.stream().
                                    filter(formulaRisk -> (GREATER_EQUAL.equals(formulaRisk.getFormula()) && parseDoubleOrDefault(formulaResult) >= formulaRisk.getDefaultValue()) ||
                                            (LESS_EQUAL.equals(formulaRisk.getFormula()) && parseDoubleOrDefault(formulaResult) <= formulaRisk.getDefaultValue()) ||
                                            (GREATER.equals(formulaRisk.getFormula()) && parseDoubleOrDefault(formulaResult) > formulaRisk.getDefaultValue()) ||
                                            (LESS.equals(formulaRisk.getFormula()) && parseDoubleOrDefault(formulaResult) < formulaRisk.getDefaultValue())
                                    )
                                    .count();

                            //如果多个都满足，则比较risk大小
                            if (matchCount == groupFormulaRiskList.size()) {
                                risk.updateAndGet(currentRisk -> Math.max(currentRisk, formulaRiskGroup.getRisk()));
                            }
                        });
                    } else {
                        //存在部分按区间计算，部分单个逻辑（1.3<= FIB4 <= 2.67 AND FIB4>2.67）

                        //判断单个的是否都满足
                        List<IndexRisk> sampleFormulaRiskList = formulaRiskList.stream()
                                .filter(formulaRisk -> formulaRisk.getGroupId() == null)
                                .collect(Collectors.toList());

                        boolean sampleMatch = sampleFormulaRiskList.stream()
                                .anyMatch(formulaRisk -> (GREATER_EQUAL.equals(formulaRisk.getFormula()) && parseDoubleOrDefault(formulaResult) >= formulaRisk.getDefaultValue()) ||
                                        (LESS_EQUAL.equals(formulaRisk.getFormula()) && parseDoubleOrDefault(formulaResult) <= formulaRisk.getDefaultValue()) ||
                                        (GREATER.equals(formulaRisk.getFormula()) && parseDoubleOrDefault(formulaResult) > formulaRisk.getDefaultValue()) ||
                                        (LESS.equals(formulaRisk.getFormula()) && parseDoubleOrDefault(formulaResult) < formulaRisk.getDefaultValue())
                                );

                        if (sampleMatch) {
                            risk.updateAndGet(currentRisk -> Math.max(currentRisk, sampleFormulaRiskList.get(0).getRisk()));
                        } else {
                            //数据落在区间内
                            Map<Integer, List<IndexRisk>> groupedFormulaRiskList = formulaRiskList.stream().filter(formulaRisk -> formulaRisk.getGroupId() != null)
                                    .collect(Collectors.groupingBy(IndexRisk::getGroupId));
                            groupedFormulaRiskList.forEach((groupId, groupFormulaRiskList) -> {
                                IndexRiskGroup formulaRiskGroup = indexRiskGroupList.stream()
                                        .filter(indexRiskGroup -> indexRiskGroup.getId().equals(groupFormulaRiskList.get(0).getGroupId()))
                                        .findFirst().orElse(null);

                                //比较满足个数
                                long matchCount = groupFormulaRiskList.stream().
                                        filter(formulaRisk -> (GREATER_EQUAL.equals(formulaRisk.getFormula()) && parseDoubleOrDefault(formulaResult) >= formulaRisk.getDefaultValue()) ||
                                                (LESS_EQUAL.equals(formulaRisk.getFormula()) && parseDoubleOrDefault(formulaResult) <= formulaRisk.getDefaultValue()) ||
                                                (GREATER.equals(formulaRisk.getFormula()) && parseDoubleOrDefault(formulaResult) > formulaRisk.getDefaultValue()) ||
                                                (LESS.equals(formulaRisk.getFormula()) && parseDoubleOrDefault(formulaResult) < formulaRisk.getDefaultValue())
                                        )
                                        .count();

                                //如果多个都满足，则比较risk大小
                                if (matchCount == groupFormulaRiskList.size()) {
                                    risk.updateAndGet(currentRisk -> Math.max(currentRisk, formulaRiskGroup.getRisk()));
                                }
                            });
                        }
                    }
                } catch (ScriptException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        System.out.println(risk.get());
        return risk.get();
    }

    private static double parseDoubleOrDefault(Object value) {
        return value != null && !StringUtils.isEmpty(String.valueOf(value)) ? Double.parseDouble(String.valueOf(value)) : 0;
    }
}
