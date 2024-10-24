package com.supra.daas.service;

import java.util.List;

import com.supra.daas.api.CommonResult;
import com.supra.daas.domain.ResourceInfo;
import com.supra.daas.domain.VMSettings;
import com.supra.daas.domain.VmStatus;

public interface AdminResourceService {

    CommonResult<List<ResourceInfo>> listResource();

    CommonResult<ResourceInfo> createResource(ResourceInfo resource);

    CommonResult<ResourceInfo> updateResource(ResourceInfo resource);

    CommonResult<String> deleteResource(ResourceInfo resource);

    CommonResult<VMSettings> getVmSettings();

    CommonResult<VmStatus> getVmStatus(String id);

    CommonResult<String> vmOperate(String id, String op);
}
