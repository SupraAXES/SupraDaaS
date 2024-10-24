package com.supra.daas.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.supra.daas.domain.AccessInfo;
import com.supra.daas.domain.AccountInfo;
import com.supra.daas.domain.ResourceInfo;
import com.supra.daas.model.TaskModel;
import com.supra.daas.model.VMTemplateModel;
import com.supra.daas.repository.ResourceRepository;
import com.supra.daas.repository.TaskRepository;
import com.supra.daas.repository.VMTemplateRepository;
import com.supra.daas.service.AdminVMService;
import com.supra.daas.util.EmptyUtils;
import com.supra.daas.util.HttpUtil;
import com.supra.daas.util.IdUtil;
import com.supra.daas.util.IpUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AdminVMServiceImpl implements AdminVMService {
    
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private VMTemplateRepository templateRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    private void addTask(String category, String action,
            ResourceInfo resource, ResourceInfo old) {

        TaskModel model = new TaskModel();
        model.setCategory(category);
        model.setAction(action);
        JSONObject info = new JSONObject();
        if (resource != null) {
            info.put("resource", resource);
        }
        if (old != null) {
            info.put("old", old);
        }
        model.setInfo(info.toJSONString());
        model.setStatus(0);
        taskRepository.save(model);
    }

    @Override
    public void checkTemplates() {
        JSONObject result = sendRequest("https://vm-manager:1711/vm/",
                "list_template", new JSONObject());
        if (isResultSuccess(result)) {
            List<VMTemplateModel> oldTemplates = templateRepository.findAll();
            Map<String, VMTemplateModel> oldMap = new HashMap<>();
            for (VMTemplateModel item : oldTemplates) {
                oldMap.put(item.getId(), item);
            }

            JSONArray templates = result.getJSONArray("result");
            for (int i = 0; i < templates.size(); i++) {
                String item = templates.getString(i);
                if (oldMap.containsKey(item)) {
                    oldMap.remove(item);
                } else {
                    JSONObject param = new JSONObject();
                    param.put("template_id", item);
                    JSONObject meta = sendRequest("https://vm-manager:1711/vm/",
                            "template_meta", param);
                    if (isResultSuccess(meta)) {
                        JSONObject metaInfo = meta.getJSONObject("result");
                        VMTemplateModel template = new VMTemplateModel();
                        template.setId(item);
                        template.setName(metaInfo.getString("name"));
                        template.setOsType(EmptyUtils.getNotEmpty(metaInfo.getString("osType")));
                        template.setDiskSize(EmptyUtils.getNotEmpty(metaInfo.getString("diskSize")));
                        template.setSystemInfo(metaInfo.toJSONString());
                        templateRepository.save(template);
                    }
                }
            }
            
            oldMap.values().forEach(item -> {
                templateRepository.delete(item);
            });
        }
    }

    @Override
    public List<VMTemplateModel> getVMTemplates() {        
        return templateRepository.findAll();
    }

    @Override
    public void clearVm() {
        Set<String> ids = new HashSet<>();
        resourceRepository.findAll().forEach(resource -> {
            String vmId = "dvm-" + resource.getId();
            ids.add(vmId);
        });

        List<String> list = listVM();
        for (String item : list) {
            if (!ids.contains(item)) {
                stopVM(item);
            }
        }
    }

    @Override
    public boolean createResource(ResourceInfo resource) {
        if (EmptyUtils.isEmpty(resource.getTemplateId())) {
            return false;
        }

        VMTemplateModel template = templateRepository.findById(
                resource.getTemplateId()).get();
        if (template == null) {
            return false;
        }

        // update admin user
        if (EmptyUtils.isEmpty(resource.getPassword())) {
            resource.setPassword(IdUtil.generateShortKey());
        }

        resource.setVmMeta(template.getSystemInfo());
        resource.setVmPass(IdUtil.generateShortKey());
        resource.setStatus(1);

        resourceRepository.save(resource.toModel());

        addTask("VM", "create_vm", resource, null);

        return true;
    }

    @Override
    public boolean updateResource(ResourceInfo resource, ResourceInfo old) {
        if (EmptyUtils.isEmpty(resource.getTemplateId())) {
            return false;
        }

        VMTemplateModel template = templateRepository.findById(
                resource.getTemplateId()).get();
        if (template == null) {
            return false;
        }

        old.setVmMeta(template.getSystemInfo());
        resource.setVmMeta(template.getSystemInfo());

        addTask("VM", "update_vm", resource, old);

        return true;
    }

    @Override
    public boolean deleteResource(ResourceInfo resource) {
        addTask("VM", "delete_vm", null, resource);

        return true;
    }

    @Override
    public boolean isRunning(ResourceInfo info) {
        return checkRunning(info, null);
    }

    @Override
    public boolean checkRunning(ResourceInfo resource, String unit) {
        String vmId = "dvm-" + resource.getId();
        String admUser = resource.getUsername();
        String admPass = resource.getPassword();
        if (EmptyUtils.isEmpty(unit)) {
            unit = getManagementUnit(resource.getVmMeta());
        }

        return checkRunning(vmId, admUser, admPass, unit);
    }

    @Override
    public boolean checkExist(ResourceInfo resource) {
        String vmId = "dvm-" + resource.getId();

        List<String> list = listVM();
        return list.contains(vmId);
    }

    @Override
    public String getStatus(ResourceInfo resource, String unit) {
        String vmId = "dvm-" + resource.getId();
        String admUser = resource.getUsername();
        String admPass = resource.getPassword();
        if (EmptyUtils.isEmpty(unit)) {
            unit = getManagementUnit(resource.getVmMeta());
        }
        return getStatus(vmId, admUser, admPass, unit);
    }

    @Override
    public boolean run(ResourceInfo resource) {
        return runVM(resource, false);
    }

    @Override
    public boolean run(ResourceInfo resource, boolean guestVnc) {
        return runVM(resource, guestVnc);
    }

    @Override
    public boolean stop(ResourceInfo resource) {
        addTask("VM", "stop_vm", null, resource);
        return true;
    }

    @Override
    public boolean shutdown(ResourceInfo resource) {
        String vmId = "dvm-" + resource.getId();
        String admUser = resource.getUsername();
        String admPass = resource.getPassword();

        JSONObject info = new JSONObject();
        info.put("adm_name", EmptyUtils.getNotEmpty(admUser));
        info.put("adm_passwd", EmptyUtils.getNotEmpty(admPass));
        instanceSendRequest(vmId, "shutdown", info);
        return true;
    }

    @Override
    public boolean createVM(TaskModel model) {
        JSONObject info = JSON.parseObject(model.getInfo());
        ResourceInfo resource = info.getObject(
                "resource", ResourceInfo.class);

        VMTemplateModel template = templateRepository.findById(
                resource.getTemplateId()).get();
        if (template == null) {
            return false;
        }

        String vmId = "dvm-" + resource.getId();
        String servId = vmId + '-' + System.currentTimeMillis();

        // create machine
        List<String> list = listMachine();
        if (! list.contains(vmId)) {
            info = new JSONObject();
            info.put("machine_id", vmId);
            info.put("sys_from_type", "template");
            info.put("sys_from_id", resource.getTemplateId());
            JSONArray sizes = new JSONArray();
            if (EmptyUtils.isNotEmpty(resource.getDiskSize())
                    && !"N/A".equals(resource.getDiskSize())) {
                sizes.add(resource.getDiskSize());
            }
            info.put("data_disk_sizes", sizes);

            if (!managerSendRequest("create_machine", info)) {
                deleteVM(vmId);
                return false;
            }
        }

        // run vm
        info = getRunVmParam(resource, template.getOsType(), resource.getVmMeta());
        info.put("serv_name", servId);
        if (!managerSendRequest("run_vm", info)) {
            // deleteVM(vmId);
            return false;
        }

        String admUser = "";
        String admPass = "";
        JSONObject meta = getVmMetaJson(resource);
        if (meta != null) {
            admUser = meta.getString("adminName");
            admPass = meta.getString("adminPasswd");
        }
        if (EmptyUtils.isEmpty(resource.getUsername()) && admUser != null) {
            resource.setUsername(admUser);
        }

        if (!checkStatus(servId, admUser, admPass, meta)) {
            stopVM(servId);
            // deleteVM(vmId);
            return false;
        }

        String apiUser = "supra_api_user";
        String apiPass = resource.getVmPass();
        if (needSupraApiUser(resource)) {
            info = new JSONObject();
            info.put("adm_name", admUser);
            info.put("adm_passwd", admPass);
            info.put("name", apiUser);
            info.put("passwd", apiPass);
            if (!instanceSendRequest(servId, "new_adm_user", info)) {
                stopVM(servId);
                // deleteVM(vmId);
                return false;
            }
        } else {
            apiUser = resource.getUsername();
            apiPass = resource.getPassword();
        }

        if (EmptyUtils.isNotEmpty(resource.getUsername())) {
            info = new JSONObject();
            info.put("adm_name", admUser);
            info.put("adm_passwd", admPass);
            info.put("name", resource.getUsername());
            info.put("passwd", resource.getPassword());
            if (!instanceSendRequest(servId, "new_adm_user", info)) {
                stopVM(servId);
                // deleteVM(vmId);
                return false;
            }

            // remove old admin user
            if (!resource.getUsername().equals(admUser)) {
                info = new JSONObject();
                info.put("adm_name", apiUser);
                info.put("adm_passwd", apiPass);
                info.put("name", admUser);
                if (!instanceSendRequest(servId, "remove_adm_user", info)) {
                    stopVM(servId);
                    // deleteVM(vmId);
                    return false;
                }
            }
        }

        // rdp user
        List<AccountInfo> accounts = resource.getAutoAccounts();
        if (accounts != null) {
            for (AccountInfo account : accounts) {
                info = new JSONObject();
                info.put("adm_name", apiUser);
                info.put("adm_passwd", apiPass);
                info.put("name", account.getUser());
                info.put("passwd", account.getPass());
                if (!instanceSendRequest(servId, "new_rdp_user", info)) {
                    stopVM(servId);
                    // deleteVM(vmId);
                    return false;
                }
            }
        }

        // change resource status
        resourceRepository.findById(resource.getId()).ifPresent(item -> {
            ResourceInfo tmp = ResourceInfo.from(item);
            tmp.setStatus(0);
            tmp.setUsername(resource.getUsername());
            resourceRepository.save(tmp.toModel());
        });

        if (meta == null || (meta.getJSONObject("management") != null
                && meta.getJSONObject("management").getString("protocol") != null)) {
            info = new JSONObject();
            info.put("adm_name", apiUser);
            info.put("adm_passwd", apiPass);
            instanceSendRequest(servId, "shutdown", info);
        } else {
            stopVM(servId);
        }

        // run(resource);

        return true;
    }

    @Override
    public boolean updateVM(TaskModel model) {
        JSONObject info = JSON.parseObject(model.getInfo());
        ResourceInfo resource = info.getObject(
                "resource", ResourceInfo.class);
        ResourceInfo old = info.getObject(
                "old", ResourceInfo.class);

        String oldAdmUser = old.getUsername();
        String oldAdmPass = old.getPassword();

        Map<String, String> users = new HashMap<>();
        if (EmptyUtils.isNotEmpty(old.getAutoAccounts())) {
            for (AccountInfo account : old.getAutoAccounts()) {
                if (EmptyUtils.isNotEmpty(account.getUser())) {
                    users.put(account.getUser(), account.getPass());
                }
            }
        }

        String vmId = "dvm-" + resource.getId();
        String[] apiUser = getSupraApiUser(old);
        String mgrUnit = getManagementUnit(old.getVmMeta());

        // disk size not allow to change
        if (!getUpdateResource(resource).equals(getUpdateResource(old))) {
            stopVM(vmId);
            try {
                Thread.sleep(5000l);
            } catch (Exception e) {
            }
            runVM(resource);
        } else if (!checkRunning(vmId, oldAdmUser, oldAdmPass, mgrUnit)) {
            if (!checkExist(resource)) {
                runVM(resource);
            }
        }

        if (!checkStatus(vmId, oldAdmUser, oldAdmPass, getVmMetaJson(old))) {
            if (!checkRunning(vmId, apiUser[0], apiUser[1], mgrUnit)) {
                return false;
            }
        }

        // rdp user
        List<AccountInfo> accounts = resource.getAutoAccounts();
        if (accounts != null) {
            for (AccountInfo account : accounts) {
                if (users != null) {
                    String passwd = users.remove(account.getUser());
                    if (passwd != null && passwd.equals(account.getPass())) {
                        continue;
                    }
                }

                info = new JSONObject();
                info.put("adm_name", apiUser[0]);
                info.put("adm_passwd", apiUser[1]);
                info.put("name", account.getUser());
                info.put("passwd", account.getPass());

                if (!instanceSendRequest(vmId, "new_rdp_user", info)) {
                    return false;
                }
            }
        }

        if (EmptyUtils.isNotEmpty(users)) {
            for (String user : users.keySet()) {
                info = new JSONObject();
                info.put("adm_name", apiUser[0]);
                info.put("adm_passwd", apiUser[1]);
                info.put("name", user);
                if (!instanceSendRequest(vmId, "remove_rdp_user", info)) {
                    return false;
                }
            }
        }

        if (EmptyUtils.isNotEmpty(resource.getPassword())
                && !resource.getPassword().equals(old.getPassword())) {
            // update admin user
            info = new JSONObject();
            info.put("adm_name", apiUser[0]);
            info.put("adm_passwd", apiUser[1]);
            info.put("name", resource.getUsername());
            info.put("passwd", resource.getPassword());
            if (!instanceSendRequest(vmId, "new_adm_user", info)) {
                return false;
            }

            // remove old admin user
            if (!old.getUsername().equals(resource.getUsername())) {
                info = new JSONObject();
                info.put("adm_name", apiUser[0]);
                info.put("adm_passwd", apiUser[1]);
                info.put("name", old.getUsername());
                if (!instanceSendRequest(vmId, "remove_adm_user", info)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean deleteVM(TaskModel model) {
        JSONObject info = JSON.parseObject(model.getInfo());
        ResourceInfo resource = info.getObject(
                "old", ResourceInfo.class);
        String vmId = "dvm-" + resource.getId();

        stopVM(vmId);

        info = new JSONObject();
        info.put("machine_id", vmId);
        return managerSendRequest("delete_machine", info);
    }

    @Override
    public boolean runVM(TaskModel model) {
        JSONObject info = JSON.parseObject(model.getInfo());
        ResourceInfo resource = info.getObject(
                "old", ResourceInfo.class);
        return runVM(resource);
    }

    @Override
    public boolean stopVM(TaskModel model) {
        JSONObject info = JSON.parseObject(model.getInfo());
        ResourceInfo resource = info.getObject(
                "old", ResourceInfo.class);
        String vmId = "dvm-" + resource.getId();

        return stopVM(vmId);
    }

    private List<String> listMachine() {
        List<String> ids = new ArrayList<>();

        JSONObject param = new JSONObject();
        JSONObject result = sendRequest("https://vm-manager:1711/vm/",
                "list_machine", param);
        if (isResultSuccess(result)) {
            JSONArray array = result.getJSONArray("result");
            for (int i = 0; i < array.size(); i++) {
                String item = array.getString(i);
                ids.add(item);
            }
        }

        return ids;
    }

    private List<String> listVM() {
        List<String> vmList = new ArrayList<>();

        JSONObject param = new JSONObject();
        param.put("start_with", "dvm-");
        JSONObject result = sendRequest("https://vm-manager:1711/vm/",
                "list_vm", param);
        if (isResultSuccess(result)) {
            JSONArray array = result.getJSONArray("result");
            for (int i = 0; i < array.size(); i++) {
                JSONObject item = array.getJSONObject(i);
                String itemId = item.getString("name");
                vmList.add(itemId);
            }
        }

        return vmList;
    }

    private String[] getSupraApiUser(ResourceInfo resource) {
        if (needSupraApiUser(resource)) {
            return new String[] { "supra_api_user", resource.getVmPass() };
        }

        return new String[] { resource.getUsername(), resource.getPassword() };
    }

    private boolean needSupraApiUser(ResourceInfo resource) {
        JSONObject vmMeta = getVmMetaJson(resource);
        return vmMeta == null || (vmMeta.containsKey("userMgr")
                && vmMeta.getIntValue("userMgr") != 0);
    }

    private JSONObject getVmMetaJson(ResourceInfo resource) {
        JSONObject vmMeta = null;
        if (EmptyUtils.isNotEmpty(resource.getVmMeta())) {
            vmMeta = JSON.parseObject(resource.getVmMeta());
        }

        return vmMeta;
    }

    private String getUpdateResource(ResourceInfo resource) {
        ResourceInfo info = new ResourceInfo();
        info.setVmOpts(resource.getVmOpts());
        if (resource.getAccess() != null) {
            List<AccessInfo> infos = new ArrayList<>();
            for (AccessInfo item : resource.getAccess()) {
                AccessInfo tmp = new AccessInfo();
                tmp.setPl(item.getPl());
                tmp.setPort(item.getPort());
                infos.add(tmp);
            }
            info.setAccess(infos);
        }
        return JSON.toJSONString(info);
    }

    private String getManagementUnit(String meta) {
        String unit = "rdp";
        if (meta != null) {
            try {
                JSONObject json = JSON.parseObject(meta)
                        .getJSONObject("management");
                unit = json.getString("protocol");
                if (EmptyUtils.isEmpty(unit)) {
                    unit = "qemu";
                }
            } catch (Exception e) {
                unit = "qemu";
            }
        }

        return unit;
    }

    public boolean checkStatus(String servId, String admUser, String admPass, JSONObject meta) {
        boolean statusOk = false;
        String unit = "rdp";
        int interval = 4;

        if (meta != null) {
            try {
                JSONObject json = meta.getJSONObject("management");
                unit = json.getString("protocol");
                interval = json.getIntValue("interval");
                if (EmptyUtils.isEmpty(unit)) {
                    unit = "qemu";
                }
            } catch (Exception e) {
                unit = "qemu";
            }
        }

        try {
            for (int i = 0; i < 120; i++) {
                if (checkRunning(servId, admUser, admPass, "qemu")) {
                    statusOk = true;
                    break;
                }
                Thread.sleep(1000l);
            }

            if (statusOk && !"qemu".equals(unit)) {
                statusOk = false;
                for (int i = 0; i < 10; i++) {
                    if (checkRunning(servId, admUser, admPass, unit)) {
                        statusOk = true;
                        break;
                    }
                    Thread.sleep(1000l * (interval <= 0 ? 4 : interval));
                }
            }
        } catch (Exception e) {
        }

        return statusOk;
    }

    private JSONObject getRunVmParam(ResourceInfo resource, String osType, String meta) {
        return getRunVmParam(resource, osType, meta, false);
    }

    private JSONObject getRunVmParam(ResourceInfo resource, String osType, String meta, boolean guestVnc) {
        String vmId = "dvm-" + resource.getId();
        JSONObject info = new JSONObject();
        info.put("serv_name", vmId);
        info.put("vm_id", vmId);
        info.put("serv_token", resource.getId());

        String vmNetwork = System.getenv().get("VM_NETWORK");
        if (EmptyUtils.isEmpty(vmNetwork)) {
            vmNetwork = "supra_projectors";
        }
        info.put("container_network", vmNetwork);
        info.put("os_type", osType);
        info.put("vm_id_type", "machine");

        JSONObject opts = resource.getVmOpts();
        if (opts == null) {
            opts = new JSONObject();
        }
        opts.put("keep_report_quitting", false);

        if (EmptyUtils.isNotEmpty(resource.getIpMask())) {
            String[] ipMasks = resource.getIpMask().split("/");
            int mask = 24;
            if (ipMasks.length == 2) {
                mask = Integer.parseInt(ipMasks[1]);
                if (mask < 0 || mask > 32) {
                    mask = 24;
                }
            }
            long ip = IpUtil.getIpFromString(ipMasks[0]) >> (32 - mask) << (32 - mask);
            StringBuffer ipMask = new StringBuffer();
            ipMask.append(IpUtil.getIpFromLong(ip)).append("/").append(mask)
                    .append(",dhcpstart=").append(ipMasks[0]);
            opts.put("guest_lan", ipMask.toString());
        }

        List<String> tcpPorts = new ArrayList<>();
        if (meta == null) {
            tcpPorts.add("\"127.0.0.1:5985:5985\"");
        } else if (meta.length() > 0) {
            try {
                JSONObject json = JSONObject.parseObject(meta).getJSONObject("management");
                String port = json.getString("port");
                if (EmptyUtils.isNotEmpty(port)) {
                    tcpPorts.add(new StringBuffer().append("\"127.0.0.1:")
                            .append(port).append(":")
                            .append(port).append('"').toString());
                }
            } catch (Exception e) {
            }
        }

        if (meta == null && EmptyUtils.isEmpty(resource.getAccess())) {
            tcpPorts.add("\"0.0.0.0:3389:3389\"");
        }

        if (resource.getAccess() != null) {
            Map<String, String> plPort = new HashMap<>();

            try {
                JSONArray access = JSON.parseObject(resource.getVmMeta()).getJSONArray("access");
                for (int i = 0; i < access.size(); i++) {
                    JSONObject item = access.getJSONObject(i);
                    plPort.put(item.getString("protocol"), item.getString("port"));
                }
            } catch (Exception e) {
            }

            for (AccessInfo item : resource.getAccess()) {
                String port = plPort.get(item.getPl());
                if ("guest_vnc".equals(item.getPl())) {
                    guestVnc = true;
                    continue;
                }

                if (EmptyUtils.isNotEmpty(item.getPort())) {
                    port = item.getPort();
                }
                if (EmptyUtils.isEmpty(port)) {
                    String pl = item.getPl();
                    if ("ssh".equals(pl)) {
                        port = "22";
                    } else if ("vnc".equals(pl)) {
                        port = "5900";
                    } else if ("http".equals(pl)) {
                        port = "80";
                    } else if ("https".equals(pl)) {
                        port = "443";
                    } else {
                        port = "3389";
                    }
                }
                tcpPorts.add(new StringBuffer().append("\"0.0.0.0:")
                        .append(port).append(":")
                        .append(port).append('"').toString());
            }
        }
        if (tcpPorts.size() > 0) {
            opts.put("tcp_ports", "(" + String.join(" ", tcpPorts) + ")");
        }

        if (guestVnc) {
            opts.put("guest_vnc", true);
        }

        info.put("vm_opts", opts);
        return info;
    }

    private boolean runVM(ResourceInfo resource) {
        return runVM(resource, false);
    }

    private boolean runVM(ResourceInfo resource, boolean guestVnc) {
        VMTemplateModel template = templateRepository.findById(
                resource.getTemplateId()).get();

        if (template == null) {
            return false;
        }

        // run vm
        JSONObject info = getRunVmParam(resource, template.getOsType(), resource.getVmMeta(), guestVnc);

        return managerSendRequest("run_vm", info);
    }

    private boolean stopVM(String servId) {
        JSONObject info = new JSONObject();
        info.put("serv_name", servId);
        return managerSendRequest("stop_vm", info);
    }

    private boolean deleteVM(String vmId) {
        JSONObject info = new JSONObject();
        info.put("machine_id", vmId);
        return managerSendRequest("delete_machine", info);
    }

    private boolean managerSendRequest(String action, JSONObject param) {
        JSONObject result = sendRequest("https://vm-manager:1711/vm/",
                action, param);
        return isResultSuccess(result);
    }

    private boolean instanceSendRequest(String owner, String action, JSONObject param) {
        JSONObject result = sendRequest("http://" + owner + ":12345",
                action, param);
        return isResultSuccess(result);
    }

    private boolean checkRunning(String vmId, String admUser, String admPass, String unit) {
        return "Running".equals(getStatus(vmId, admUser, admPass, unit));
    }

    private String getStatus(String vmId, String admUser, String admPass, String unit) {
        JSONObject param = new JSONObject();
        param.put("adm_name", EmptyUtils.getNotEmpty(admUser));
        param.put("adm_passwd", EmptyUtils.getNotEmpty(admPass));
        param.put("unit", unit);
        JSONObject content = sendRequest("http://" + vmId + ":12345", "status", param);
        if (content == null) {
            return null;
        }
        if (content.containsKey("error")) {
            return null;
        }
        return content.getString("result");
    }

    private boolean isResultSuccess(JSONObject json) {
        if (json == null) {
            return false;
        }
        if (json.containsKey("error")) {
            return false;
        }
        return true;
    }

    private JSONObject sendRequest(String url, String method, JSONObject pMaps) {
        JSONObject json = new JSONObject();
        json.put("jsonrpc", "2.0");
        json.put("method", method);

        json.put("params", pMaps);
        json.put("id", System.currentTimeMillis() + pMaps.hashCode());

        String param = json.toJSONString();
        String content = HttpUtil.postJsonParams(url, param);

        JSONObject logParam = new JSONObject(pMaps);
        if (logParam.containsKey("adm_passwd")) {
            logParam.put("adm_passwd", "******");
        }
        if (logParam.containsKey("passwd")) {
            logParam.put("passwd", "******");
        }
        json.put("params", logParam);
        param = json.toJSONString();

        log.info("sendRequest: url={}, param={}, content={}", url, param, content);
        if (EmptyUtils.isEmpty(content)) {
            return null;
        }

        try {
            return JSON.parseObject(content);
        } catch (Exception e) {
            return null;
        }
    }

}
