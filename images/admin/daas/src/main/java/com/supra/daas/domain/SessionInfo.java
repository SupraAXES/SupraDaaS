package com.supra.daas.domain;

import com.alibaba.fastjson.JSON;
import com.supra.daas.model.SessionModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SessionInfo {
    
    private String uuid;

    private String resourceId;

    private String path;

    private AccountInfo autoAccount;

    private String access;

    private ResourceInfo resource;

    private ResourceRule rule;

    private int status;

    public static SessionInfo from(SessionModel model) {
        SessionInfo param = JSON.parseObject(model.getInfo(), SessionInfo.class);
        return param;
    }

    public SessionModel toModel() {
        SessionModel model = new SessionModel();
        model.setId(uuid);
        model.setResourceId(resourceId);
        model.setInfo(JSON.toJSONString(this));
        return model;
    }
}
