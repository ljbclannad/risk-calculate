package com.example.drools.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.drools.service.RiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.script.ScriptException;

@RestController
@RequestMapping("/risk")
public class RiskController {

    @Autowired
    RiskService riskService;

    @PostMapping("/calculate")
    public int calculate(@RequestBody JSONObject data) throws ScriptException {
        return riskService.calculate(data);
    }

    @GetMapping("/refresh")
    public void refresh() {
        riskService.refresh();
    }
}
