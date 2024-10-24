package com.supra.daas.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.supra.daas.api.CommonResult;
import com.supra.daas.domain.ResourceInfo;
import com.supra.daas.domain.VMSettings;
import com.supra.daas.domain.VMSettingsTemplate;
import com.supra.daas.domain.VmStatus;
import com.supra.daas.model.ResourceModel;
import com.supra.daas.model.VMTemplateModel;
import com.supra.daas.repository.ResourceRepository;
import com.supra.daas.service.AdminResourceService;
import com.supra.daas.service.AdminVMService;
import com.supra.daas.util.EmptyUtils;
import com.supra.daas.util.FileUtil;
import com.supra.daas.util.IdUtil;

@Service
public class AdminResourceServiceImpl implements AdminResourceService {
    
    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private AdminVMService adminVMService;

    @Value("${settings.path.config}")
    private String configPath;

    @Override
    public CommonResult<List<ResourceInfo>> listResource() {
        List<ResourceInfo> resources = new ArrayList<>();

        List<ResourceModel> models = resourceRepository.findAll();
        for (ResourceModel model : models) {
            resources.add(ResourceInfo.from(model));
        }

        return CommonResult.success(resources);
    }

    @Override
    public CommonResult<ResourceInfo> createResource(ResourceInfo resource) {
        resource.setId(IdUtil.generateUuid());
        resource.setAutoLogin(true);

        ResourceModel model = resource.toModel();
        resourceRepository.save(model);

        adminVMService.createResource(resource);

        return CommonResult.success(resource);
    }

    @Override
    public CommonResult<ResourceInfo> updateResource(ResourceInfo resource) {
        Optional<ResourceModel> optional = resourceRepository.findById(
            resource.getId());
        if (! optional.isPresent()) {
            return CommonResult.failed("Not Found");
        }

        ResourceInfo old = ResourceInfo.from(optional.get());

        adminVMService.updateResource(resource, old);

        old.update(resource); 
        resourceRepository.save(old.toModel());

        return CommonResult.success(resource);
    }

    @Override
    public CommonResult<String> deleteResource(ResourceInfo resource) {
        Optional<ResourceModel> optional = resourceRepository.findById(
            resource.getId());
        if (! optional.isPresent()) {
            return CommonResult.failed("Not Found");
        }

        ResourceInfo old = ResourceInfo.from(optional.get());
        adminVMService.deleteResource(old);

        resourceRepository.delete(old.toModel());

        return CommonResult.success("");
    }

    @Override
    public CommonResult<VMSettings> getVmSettings() {
        List<VMTemplateModel> templateModels = adminVMService.getVMTemplates();
        VMSettings settings = new VMSettings();
        String content = FileUtil.loadFileContent(configPath + "/vmSettings.json");
        if (EmptyUtils.isNotEmpty(content)) {
            settings = JSON.parseObject(content, VMSettings.class);
        }
        List<VMSettingsTemplate> templates = new ArrayList<>();
        for (VMTemplateModel model : templateModels) {
            templates.add(VMSettingsTemplate.from(model));
        }
        settings.setTemplates(templates);

        return CommonResult.success(settings);
    }

    @Override
    public CommonResult<VmStatus> getVmStatus(String id) {
        Optional<ResourceModel> optional = resourceRepository.findById(id);
        if (! optional.isPresent()) {
            return CommonResult.failed("Not Found");
        }

        ResourceInfo resource = ResourceInfo.from(optional.get());
        VmStatus status = new VmStatus();

        String value = adminVMService.getStatus(resource, "qemu");
        if (value != null) {
            status.setQemu(value);
            value = adminVMService.getStatus(resource, null);
            status.setVm(value);
        }
        return CommonResult.success(status);
    }

    @Override
    public CommonResult<String> vmOperate(String id, String action) {
        Optional<ResourceModel> optional = resourceRepository.findById(id);
        if (! optional.isPresent()) {
            return CommonResult.failed("Not Found");
        }

        ResourceInfo resource = ResourceInfo.from(optional.get());

        if ("run".equals(action)) {
            adminVMService.run(resource, true);
        } else if ("stop".equals(action)) {
            if (adminVMService.checkRunning(resource, null)) {
                adminVMService.shutdown(resource);
            } else {
                adminVMService.stop(resource);
            }
        }
        
        return CommonResult.success("");
    }

}
