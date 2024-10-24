package com.supra.daas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.supra.daas.api.CommonResult;
import com.supra.daas.domain.ResourceInfo;
import com.supra.daas.domain.SimpleSessionInfo;
import com.supra.daas.service.SessionService;

@Controller
@RequestMapping("/connect")
public class ConnectController {
    
    @Autowired
    private SessionService sessionService;

    @RequestMapping(value = "/open", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<String> open(@RequestBody ResourceInfo info) {
        return sessionService.openConnect(info);
    }

    @RequestMapping(value = "/open/info", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<SimpleSessionInfo> openInfo(@RequestParam(value = "id") String id) {
        return sessionService.openConnectInfo(id);
    }

    @RequestMapping(value = "/open/check", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<Integer> openConnectCheck(@RequestParam(value = "id") String id) {
        return sessionService.openConnectCheck(id);
    }

    @RequestMapping(value = "/open/alive", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<Integer> openConnectAlive(@RequestParam(value = "id") String id) {
        return sessionService.openConnectAlive(id);
    }

    @RequestMapping(value = "/open/updateResolution", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<Integer> updateResolution(
            @RequestParam(value = "id") String id,
            @RequestParam(value = "width") int width,
            @RequestParam(value = "height") int height) {
        return sessionService.updateResolution(id, width, height);
    }
}
