package com.supra.daas.domain;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.supra.daas.util.EmptyUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SimpleResourceInfo {
    
    private String id;
    
    private String name;

    private String icon;

    private String group;

    private Integer loginRequired;

    private List<String> access;

    public SimpleResourceInfo(ResourceInfo resource) {
        this.id = resource.getId();
        this.name = resource.getName();
        this.icon = resource.getIcon();
        this.group = resource.getGroup();
        this.loginRequired = 0;
        if (EmptyUtils.isNotEmpty(resource.getAutoLogin())) {
            this.loginRequired = resource.findAutoAccount() != null ? 0 : 1;
        }
        this.access = new ArrayList<>();
        if (EmptyUtils.isNotEmpty(resource.getAccess())) {
            if (resource.findDefaultAccess() == null) {
                for (AccessInfo access : resource.getAccess()) {
                    String pl = access.getPl();
                    if (EmptyUtils.isNotEmpty(access.getPort())) {
                        pl += ":" + access.getPort();
                    }
                    this.access.add(pl);
                }
            }
        }
    }

}
