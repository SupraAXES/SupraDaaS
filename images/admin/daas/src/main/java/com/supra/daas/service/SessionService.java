package com.supra.daas.service;

import com.supra.daas.api.CommonResult;
import com.supra.daas.domain.ResourceInfo;
import com.supra.daas.domain.SessionInfo;
import com.supra.daas.domain.SimpleSessionInfo;

public interface SessionService {

    SessionInfo getSession(String id);
    
    CommonResult<String> openConnect(ResourceInfo resource);

    CommonResult<SimpleSessionInfo> openConnectInfo(String id);

    CommonResult<Integer> openConnectCheck(String id);

    CommonResult<Integer> openConnectAlive(String id);

    CommonResult<Integer> updateResolution(String id, int width, int height);
    
}
