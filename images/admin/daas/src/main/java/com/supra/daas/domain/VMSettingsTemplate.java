package com.supra.daas.domain;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.supra.daas.model.VMTemplateModel;
import com.supra.daas.util.EmptyUtils;

import lombok.Data;

@Data
public class VMSettingsTemplate {
    
    private String id;

    private String name;

    private List<String> access;

    private List<AccountInfo> users;

    private String admin;

    private Integer userMgr;

    public static VMSettingsTemplate from(VMTemplateModel model) {
        VMSettingsTemplate info = new VMSettingsTemplate();
        info.setId(model.getId());
        info.setName(model.getName());

        JSONObject systemInfo = JSON.parseObject(model.getSystemInfo());
        info.setAdmin(EmptyUtils.getNotEmpty(systemInfo.getString("adminName")));
        info.setUserMgr(systemInfo.getInteger("userMgr"));

        List<String> access = new ArrayList<>();
        JSONArray accessArray = systemInfo.getJSONArray("access");
        if (accessArray != null) {
            for (int i = 0; i < accessArray.size(); i++) {
                access.add(accessArray.getJSONObject(i).getString("protocol"));
            }
        }
        info.setAccess(access);

        List<AccountInfo> users = new ArrayList<>();
        JSONArray usersArray = systemInfo.getJSONArray("users");
        if (usersArray != null) {
            for (int i = 0; i < usersArray.size(); i++) {
                JSONObject user = usersArray.getJSONObject(i);
                AccountInfo accountInfo = new AccountInfo();
                accountInfo.setUser(user.getString("user"));
                accountInfo.setPass(user.getString("pass"));
                users.add(accountInfo);
            }
        }
        info.setUsers(users);

        return info;
    }
}
