package com.example.drools.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RuleResult {

    private double finallyMoney;
    private double moneySum;
    private List<String> promoteName = new ArrayList<>();

    public void setPromoteName(String promoteName) {
        this.promoteName.add(promoteName);
    }
}
