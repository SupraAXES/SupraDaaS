package com.supra.daas.service;

import java.util.List;

import com.supra.daas.api.CommonResult;
import com.supra.daas.domain.ResourceInfo;
import com.supra.daas.domain.SimpleResourceInfo;

public interface ResourceService {

    ResourceInfo getResource(String id);

    CommonResult<List<SimpleResourceInfo>> listResource();
    
}
