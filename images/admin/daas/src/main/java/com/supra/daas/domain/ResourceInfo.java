package com.supra.daas.domain;

import java.util.List;

import org.springframework.beans.BeanUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.supra.daas.model.ResourceModel;
import com.supra.daas.util.EmptyUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ResourceInfo {
    
    private String id;
    
    private String name;

    private String icon;

    private String group;

    private String username;

    private String password;

    private Boolean autoLogin;

    private Integer status; // 1 disable, -1 disconnect

    private String protocol;

    private List<AccountInfo> autoAccounts;

    private List<AccessInfo> access;

    private String ipMask;

    private JSONObject vmOpts;

    private String templateId;

    private String diskSize;

    private String vmPass;

    private String vmMeta; // vm meta

    private Integer createTime; // create time
 
    private WindowSize windowSize; // Connect Open Window Size

    public static ResourceInfo from(ResourceModel model) {
        ResourceInfo resource = JSON.parseObject(model.getInfo(), ResourceInfo.class);
        resource.setCreateTime((int) (model.getCreateTime().getTime() / 1000));
        return resource;
    }

    public ResourceModel toModel() {
        ResourceModel model = new ResourceModel();
        model.setId(id);
        model.setName(name);
        model.setIcon(EmptyUtils.getNotEmpty(icon));
        model.setInfo(JSON.toJSONString(this));
        return model;
    }

    public void update(ResourceInfo info) {
        Integer oldCreateTime = createTime; // keep old createTime
        String oldId = id;
        String oldVmPass = vmPass;
        String oldVmMeta = vmMeta;

        BeanUtils.copyProperties(info, this);

        this.createTime = oldCreateTime;
        this.id = oldId;
        this.vmPass = oldVmPass;
        this.vmMeta = oldVmMeta;
    }

    public AccountInfo findAutoAccount() {
        if (EmptyUtils.isNotEmpty(autoLogin)) {
            if (EmptyUtils.isNotEmpty(autoAccounts)) {
                for (AccountInfo account : autoAccounts) {
                    if (EmptyUtils.isNotEmpty(account.getDef())) {
                        return account;
                    }
                }
            }
        }
        return null;
    }

    public AccessInfo findDefaultAccess() {
        if (EmptyUtils.isNotEmpty(access)) {
            for (AccessInfo access : access) {
                if (EmptyUtils.isNotEmpty(access.getDef())) {
                    return access;
                }
            }
        }
        return null;
    }
}
