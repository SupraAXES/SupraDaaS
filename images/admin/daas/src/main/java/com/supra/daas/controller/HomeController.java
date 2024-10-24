package com.supra.daas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.supra.daas.api.CommonResult;
import com.supra.daas.domain.SimpleResourceInfo;
import com.supra.daas.service.ResourceService;

@Controller
@RequestMapping("/home")
public class HomeController {
    
    @Autowired
    private ResourceService resourceService;

    @RequestMapping(value = "/resource/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<SimpleResourceInfo>> resourceList() {
        return resourceService.listResource();
    }

}
