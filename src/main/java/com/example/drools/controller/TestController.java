package com.example.drools.controller;

import com.example.drools.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    TestService testService;

    @RequestMapping("/greeting")
    public String greeting() {
        return "index";
    }


    @RequestMapping("/shop")
    public String shop() {
        return "shopping";
    }


    /**
     * 编辑促销活动
     *
     * @return 结果
     */
    @GetMapping(value = "/ediePromote")
    @ResponseBody
    public void addPromote(@RequestParam String money, @RequestParam String ruleName) {
        testService.ediePromoteMap(money, ruleName);
    }

    /**
     * 购物车
     *
     * @return 返回结果
     */
    @RequestMapping(value = "/toShopping", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public HashMap<String, Object> toShopping(String money) {
        return testService.toShopping(money);
    }
}
