package com.supra.daas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.supra.daas.api.CommonResult;
import com.supra.daas.domain.ResourceInfo;
import com.supra.daas.domain.VMSettings;
import com.supra.daas.domain.VmStatus;
import com.supra.daas.service.AdminResourceService;


@Controller
@RequestMapping("/admin/resource")
public class AdminResourceController {
    
    @Autowired
    private AdminResourceService resourceService;

    @RequestMapping(value = "/vmSettings", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<VMSettings> vmSettings() {
        return resourceService.getVmSettings();
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<ResourceInfo>> list() {
        return resourceService.listResource();
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<ResourceInfo> create(
            @RequestBody ResourceInfo resource) {
        return resourceService.createResource(resource);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<ResourceInfo> update(
            @RequestBody ResourceInfo resource) {
        return resourceService.updateResource(resource);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<String> delete(
            @RequestBody ResourceInfo resource) {
        return resourceService.deleteResource(resource);
    }

    @RequestMapping(value = "/vm/status", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<VmStatus> vmStatus(@RequestParam(name = "id") String id) {
        return resourceService.getVmStatus(id);
    }

    @RequestMapping(value = "/vm/op", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<String> vmOperate(@RequestParam(name = "id") String id,
            @RequestParam(name = "action") String op) {
        return resourceService.vmOperate(id, op);
    }
}
