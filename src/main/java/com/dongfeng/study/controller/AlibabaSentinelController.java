package com.dongfeng.study.controller;

import com.dongfeng.study.bean.base.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Alibaba sentinel 测试
 *
 * @author eastFeng
 * @date 2020/8/17 - 10:38
 */
@Slf4j
@RestController
@RequestMapping("/alibaba/sentinel")
public class AlibabaSentinelController {

    @GetMapping("/test")
    public Response<String> test(){
        return Response.successInstance("----Alibaba sentinel test----");
    }
}
