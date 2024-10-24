package com.supra.daas.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.supra.daas.api.CommonResult;
import com.supra.daas.domain.ResourceInfo;
import com.supra.daas.domain.SimpleResourceInfo;
import com.supra.daas.model.ResourceModel;
import com.supra.daas.repository.ResourceRepository;
import com.supra.daas.service.ResourceService;
import com.supra.daas.util.EmptyUtils;

@Service
public class ResourceServiceImpl implements ResourceService {
    
    @Autowired
    private ResourceRepository resourceRepository;

    @Override
    public ResourceInfo getResource(String id) {
        Optional<ResourceModel> optional = resourceRepository.findById(id);
        if (optional.isPresent()) {
            return ResourceInfo.from(optional.get());
        }
        
        return null;
    }

    @Override
    public CommonResult<List<SimpleResourceInfo>> listResource() {
        List<SimpleResourceInfo> list = new ArrayList<>();

        List<ResourceModel> models = resourceRepository.findAll();
        for (ResourceModel model : models) {
            ResourceInfo resource = ResourceInfo.from(model);
            if (! EmptyUtils.isNotEmpty(resource.getStatus())) {
                list.add(new SimpleResourceInfo(resource));
            }
        }

        return CommonResult.success(list);
    }

}
