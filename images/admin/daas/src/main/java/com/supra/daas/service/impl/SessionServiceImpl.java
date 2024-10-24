package com.supra.daas.service.impl;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.supra.daas.api.CommonResult;
import com.supra.daas.domain.AccessInfo;
import com.supra.daas.domain.AccountInfo;
import com.supra.daas.domain.ResourceInfo;
import com.supra.daas.domain.ResourceRule;
import com.supra.daas.domain.SessionInfo;
import com.supra.daas.domain.SimpleSessionInfo;
import com.supra.daas.model.SessionModel;
import com.supra.daas.repository.SessionRepository;
import com.supra.daas.service.AdminVMService;
import com.supra.daas.service.ResourceService;
import com.supra.daas.service.SessionService;
import com.supra.daas.util.EmptyUtils;
import com.supra.daas.util.FileUtil;
import com.supra.daas.util.IdUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SessionServiceImpl implements SessionService {
    
    private static SessionService instance = null;

    public SessionServiceImpl() {
        instance = this;
    }

    public static SessionService getInstance() {
        return instance;
    }

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private AdminVMService vmService;

    @Value("${settings.path.files}")
    private String filesPath;

    @Value("${settings.path.config}")
    private String configPath;

    @Override
    public SessionInfo getSession(String id) {
        Optional<SessionModel> optional = sessionRepository.findById(id);
        if (optional.isPresent()) {
            SessionModel model = optional.get();
            return SessionInfo.from(model);
        }

        return null;
    }

    @Override
    public CommonResult<String> openConnect(ResourceInfo resource) {
        log.info("Open Connect: " + JSON.toJSONString(resource));

        SessionInfo info = new SessionInfo();
        info.setUuid(IdUtil.generateUuid());
        info.setResourceId(resource.getId());

        ResourceInfo resourceInfo = resourceService.getResource(resource.getId());
        if (resourceInfo == null) {
            return CommonResult.failed();
        }
        if (EmptyUtils.isNotEmpty(resource.getProtocol())) {
            info.setAccess(resource.getProtocol());
        } else {
            AccessInfo access = resource.findDefaultAccess();
            if (access != null) {
                info.setAccess(access.getPl() + ":" + access.getPort());
            }
        }
        if (EmptyUtils.isNotEmpty(resource.getAutoAccounts())) {
            info.setAutoAccount(resource.getAutoAccounts().get(0));
        } else {
            info.setAutoAccount(resource.findAutoAccount());
        }
        resourceInfo.setWindowSize(resource.getWindowSize());
        info.setResource(resourceInfo);

        String ruleContent = FileUtil.loadFileContent(configPath + "/rule.json");
        if (ruleContent != null) {
            info.setRule(JSON.parseObject(ruleContent, ResourceRule.class));
        }

        if (! vmService.isRunning(resourceInfo)) {
            if (! vmService.checkExist(resourceInfo)) {
                vmService.run(resourceInfo);
            }
        }

        File file = new File(filesPath, resource.getId());
        if (! file.exists()) {
            file.mkdirs();
        }
        info.setPath(file.getPath());

        sessionRepository.save(info.toModel());

        log.info("Open Connect: " + info.getUuid());

        return CommonResult.success(info.getUuid());
    }

    @Override
    public CommonResult<SimpleSessionInfo> openConnectInfo(String id) {
        SessionInfo session = getSession(id);
        if (session != null) {
            return CommonResult.success(new SimpleSessionInfo(session));
        }

        return CommonResult.failed();
    }

    @Override
    public CommonResult<Integer> openConnectCheck(String id) {
        return CommonResult.success(0);
    }

    @Override
    public CommonResult<Integer> openConnectAlive(String id) {
        Optional<SessionModel> optional = sessionRepository.findById(id);
        if (optional.isPresent()) {
            SessionModel model = optional.get();
            model.setUpdateTime(Calendar.getInstance().getTime());
            sessionRepository.save(model);
        }

        return CommonResult.success(0);
    }

    @Override
    public CommonResult<Integer> updateResolution(String id, int width, int height) {
        return CommonResult.success(0);
    }

}
